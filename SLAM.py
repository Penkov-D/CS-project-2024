import cv2
import numpy as np
from collections import defaultdict
from scipy.spatial.transform import Rotation as R

# RANSAC parameters
RANSAC_PROB = 0.99         # Confidance level for RANSAC
RANSAC_THRESHOLD = 1       # Maximum distance from point to epipolar line

class SLAM:
    def __init__(self, K, save_new_points = True):
        """
        Initialize the SLAM system with the camera intrinsic matrix K.
        """
        self.K = K
        self.K_inv = np.linalg.inv(K)
        
        self.R_prev, self.t_prev, self.des_prev, self.kp_prev = None, None, None, None
        
        self.world_points = np.array([])  # Global list of triangulated 3D points
        self.world_descriptors = np.array([])  # Global list of triangulated 3D points descriptors
        
        self.scale = 1
        self.save_new_points = save_new_points

        self.sift = cv2.SIFT_create()
        self.matcher = cv2.BFMatcher()
    
    # def __init__(self, K, world_points, world_descriptors, R_prev, t_prev, des_prev, kp_prev, scale = 1, save_new_points = True):
    #     """
    #     Initialize the SLAM system with an existing state.
    #     """
    #     # Intrinsic matrix.
    #     self.K = K
    #     self.K_inv = np.linalg.inv(K)

    #     # Previous frame position and interest points.
    #     self.R_prev, self.t_prev, self.des_prev, self.kp_prev = R_prev, t_prev, des_prev, kp_prev

    #     self.world_points = world_points  # Global list of triangulated 3D points
    #     self.world_descriptors = world_descriptors  # Global list of triangulated 3D points descriptors

    #     self.scale = scale
    #     self.save_new_points = save_new_points

    #     self.sift = cv2.SIFT_create()
    #     self.matcher = cv2.BFMatcher()

    def getRt(self, curr_frame):
        """
        Process the frame to compute the relative R,t with respect to the first frame.
        Steps:
            a. Compute the Fundamental and Essential matrices between consecutive frames.
            b. Refine poses using PnP and saved 3d points.
            c. Triangulate new 3D points.
            d. Add new points to the global 3D points.
            e. Return R, t of the frame.
        """
        # Detect keypoints and descriptors in the current frame.
        gray_curr_frame = cv2.cvtColor(curr_frame, cv2.COLOR_BGR2GRAY)
        kp_curr, des_curr = self.sift.detectAndCompute(gray_curr_frame, None)

        if self.R_prev is not None:
            # Match features between the previous and current frames.
            matches_curr_prev = self._find_matches(self.des_prev, des_curr)
            pts_2d_prev, pts_2d_curr = self._matches_to_points(self.kp_prev, kp_curr, matches_curr_prev)

            # Compute the Essential Matrix.
            E, mask = cv2.findEssentialMat(pts_2d_prev, pts_2d_curr, self.K, cv2.RANSAC, RANSAC_PROB, RANSAC_THRESHOLD)

            # Filter keypoints and matches if they approve the Essential Matrix.
            good_matches_curr_prev = [match for match, accepted in zip(matches_curr_prev, mask) if accepted]
            inliers_pts_2d_prev, inliers_pts_2d_curr = self._matches_to_points(self.kp_prev, kp_curr, good_matches_curr_prev)

            # Recover the camera pose (R, t), relative to the previous frame.
            _, R_relative, t_relative, _ = cv2.recoverPose(E, inliers_pts_2d_prev, inliers_pts_2d_curr, self.K)

            # Get an approximation of the position matrix of the current frame.
            R_world_estimated = R_relative @ self.R_prev
            t_world_estimated = (R_relative @ self.t_prev) + t_relative
            P_curr_estimated = self.K @ np.hstack((R_world_estimated, t_world_estimated))
            
            # Refine the position matrix with the 3d points cloud, using PnP.
            P_curr_refined, R_world, t_world, global_matches = self._refine_P(P_curr_estimated, R_world_estimated, t_world_estimated, kp_curr, des_curr)
                        
            if self.save_new_points:
                # Find the matches between prev and curr frames, that do not refer to known 3d points.
                new_global_matches = self._find_new_global_matches(good_matches_curr_prev, global_matches, len(des_curr))
                new_pts_2d_prev, new_pts_2d_curr = self._matches_to_points(self.kp_prev, kp_curr, new_global_matches)
                
                # Triangulate new 3d points.
                P_prev = self.K @ np.hstack((self.R_prev, self.t_prev))
                new_global_3d_points = self._triangulate(new_pts_2d_prev, new_pts_2d_curr, P_prev, P_curr_refined)
                new_global_3d_des = np.array([des_curr[match.trainIdx] for match in new_global_matches])

                # add the new 3D points to the global points
                self._add_new_points(new_global_3d_points, new_global_3d_des)

            self.kp_prev, self.des_prev, self.R_prev, self.t_prev = kp_curr, des_curr, R_world, t_world
            
            return R_world, t_world / self.scale
        else:
            # For the first frame, there is no relative pose (identity transformation)
            self.kp_prev, self.des_prev, self.R_prev, self.t_prev = kp_curr, des_curr, np.eye(3), np.zeros((3, 1))
            return np.eye(3), np.zeros((3, 1))

    def _find_matches(self, dsc1 : np.ndarray, dsc2 : np.ndarray) -> np.ndarray:
        """
        Finds matches between the Key points of the two images, based on the distance of their descriptors.
        Uses the Ratio Test to reduce False Positive results.

        Args:
            dsc1 (np.ndarray): Descriptors of the first image's key points.
            dsc2 (np.ndarray): Descriptors of the second image's key points.

        Returns:
            np.ndarray: The matches found.
        """
        matches = self.matcher.knnMatch(dsc1, dsc2, k = 2)
        
        good_matches = []
        for m, n in matches:
            if m.distance < 0.75 * n.distance:
                good_matches.append(m)

        good_matches = sorted(good_matches, key = lambda x: -x.distance)
        return good_matches
    
    def _matches_to_points(self, kp1 : np.ndarray, kp2 : np.ndarray, matches : np.ndarray) -> tuple[np.ndarray, np.ndarray]:
        """
        Converts a list of matches to two lists of the corresponding Key points.

        Args:
            kp1 (np.ndarray): The Key points of the first image.
            kp2 (np.ndarray): The Key points of the second image.
            matches (np.ndarray): The matches between the Key points. 

        Returns:
            tuple[np.ndarray, np.ndarray]: Two lists of the match points, one for each image.
        """
        points1 = np.array([kp1[match.queryIdx].pt for match in matches], dtype=np.float64).reshape(-1, 1, 2)
        points2 = np.array([kp2[match.trainIdx].pt for match in matches], dtype=np.float64).reshape(-1, 1, 2)
        return points1, points2

    def _triangulate(self, pts1, pts2, P1, P2):
        """
        Triangulates 3D points from matched keypoints using the essential matrix.
        
        Parameters:
        
        Returns:
        points3D (numpy.ndarray): Triangulated 3D points.
        """                
        pts1_undistorted = cv2.undistortPoints(pts1, self.K, None)
        pts2_undistorted = cv2.undistortPoints(pts2, self.K, None)
        
        # pts1_undistorted = cv2.undistortPoints(np.expand_dims(pts1, axis = 1), self.K, None)
        # pts2_undistorted = cv2.undistortPoints(np.expand_dims(pts2, axis = 1), self.K, None)
        
        points4D = cv2.triangulatePoints(P1, P2, pts1_undistorted, pts2_undistorted)
        points3D = points4D[:3] / points4D[3]
        
        return points3D.T

    def _find_new_global_matches(self, matches_curr_prev, global_matches, num_pts_2d_curr):
        """
        .
        """
        new_2d_pts_curr = np.full(num_pts_2d_curr, True)
        for g_match in global_matches:
            new_2d_pts_curr[g_match.trainIdx] = True
        return [match for match in matches_curr_prev if new_2d_pts_curr[match.trainIdx]]

    def _refine_P(self, P_estimate, R_estimate, t_estimate, kp_new, des_new):
        """
        Refines the camera projection matrix P_estimate based on known 3D points and matches keypoints.
        
        Parameters:
        - P_estimate: Initial estimate of the camera projection matrix (3x4 numpy array)
        - kp_new: List of keypoints detected in the new frame
        - des_new: Descriptors of the keypoints from the new frame
        
        Returns:
        - refined_P: Refined camera projection matrix (3x4 numpy array)
        - matches: List of good matches used in the refinement
        """
        # Match descriptors between new frame and world points
        good_matches = self._find_matches(self.world_descriptors, des_new)

        if len(good_matches) < 4:
            print("Not enough matches to refine P")
            return P_estimate, R_estimate, t_estimate, good_matches

        # Extract matched 2D image points and 3D world points
        matched_world_points = np.array([self.world_points[m.queryIdx] for m in good_matches])
        matched_image_points = np.array([kp_new[m.trainIdx].pt for m in good_matches])

        # Ensure the points are in the correct shape and type
        matched_image_points = matched_image_points.astype(np.float32).reshape(-1, 2)
        matched_world_points = matched_world_points.astype(np.float32).reshape(-1, 3)

        # Convert rotation matrix to rotation vector
        R_vec_estimate, _ = cv2.Rodrigues(R_estimate)

        # Step 4: Use solvePnP with initial estimates to refine R and t
        success, rvec_refined, tvec_refined = cv2.solvePnP(
            matched_world_points, matched_image_points, self.K, distCoeffs=np.zeros((4, 1)), 
            rvec=R_vec_estimate, tvec=t_estimate, useExtrinsicGuess=True, flags=cv2.SOLVEPNP_ITERATIVE
        )

        if not success:
            print("solvePnP failed to refine P")
            return P_estimate, R_estimate, t_estimate, good_matches

        # Convert rotation vector back to rotation matrix
        R_refined, _ = cv2.Rodrigues(rvec_refined)
        t_refined = tvec_refined.reshape(3, 1)
        
        # Step 5: Build the refined projection matrix
        P_refined = self.K @ np.hstack((R_refined, t_refined))

        return P_refined, R_refined, t_refined, good_matches

    def _add_new_points(self, new_global_3d_points, new_global_3d_des):
        """
        Add newly triangulated 3D points to the global 3D point set.
        """
        self.world_points = np.vstack((self.world_points, new_global_3d_points)) if self.world_points.size else new_global_3d_points
        self.world_descriptors = np.vstack((self.world_descriptors, new_global_3d_des)) if self.world_descriptors.size else new_global_3d_des

    def _Rt_from_P(self, P):
        Rt = self.K_inv @ P
        return Rt[:, :3], Rt[:, 3]
    
# Example usage:

K=np.eye(3)
slam = SLAM(K)
cam = cv2.VideoCapture(0)

while cv2.waitKey(20) != ord('q'):
    ret, frame = cam.read()

    if ret:
        R, t = slam.getRt(frame)
        print(t)
        cv2.imshow("cam0", frame)
