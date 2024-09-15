import cv2
import numpy as np
import pickle  # for saving and loading class state

# RANSAC parameters for robustness
RANSAC_PROB = 0.99         # Confidence level for RANSAC
RANSAC_THRESHOLD = 1       # Maximum distance from point to epipolar line

class SLAM:
    def __init__(self, intrinsic_matrix: np.ndarray, save_new_points: bool = True):
        """
        Initialize the SLAM system with a given camera intrinsic matrix.

        Args:
            intrinsic_matrix (np.ndarray): Camera intrinsic matrix (K).
            save_new_points (bool): Whether to save newly triangulated points to the global map.
        """
        self._K = intrinsic_matrix
        self._K_inv = np.linalg.inv(intrinsic_matrix)
        
        # Store previous frame's information
        self._R_prev = None
        self._t_prev = None
        self._des_prev = None
        self._kp_prev = None
        
        # Global 3D map points and their descriptors
        self._world_points = np.array([])
        self._world_descriptors = np.array([])
        
        # Scale factor for adjusting world position
        self._scale = 1
        
        # Flag to save new points
        self._save_new_points = save_new_points
        
        # Initialize SIFT detector and BFMatcher
        self._sift = cv2.SIFT_create()
        self._matcher = cv2.BFMatcher()
        
        # Unmatched and matched keypoints
        self._unmatched_indices = []
    
    def set_intrinsic_matrix(self, intrinsic_matrix: np.ndarray):
        """Sets a new intrinsic matrix."""
        self._K = intrinsic_matrix
        self._K_inv = np.linalg.inv(intrinsic_matrix)

    def get_scale(self) -> float:
        """Returns the scale factor."""
        return self._scale

    def set_scale(self, scale: float):
        """Sets a new scale factor."""
        self._scale = scale

    def get_world_points(self) -> np.ndarray:
        """Returns the current global 3D points in the map."""
        return self._world_points

    def get_world_descriptors(self) -> np.ndarray:
        """Returns the current descriptors of the global 3D points."""
        return self._world_descriptors

    def get_unmatched(self) -> list:
        """
        Get the unmatched 2D points from the last frame.

        Returns:
            list: Indices of unmatched points from the last frame.
        """
        return self._unmatched_indices

    def get_matched(self) -> list:
        """
        Get the matched 2D points from the last frame.

        Returns:
            list: Indices of matched points from the last frame.
        """
        return np.logical_not(self._unmatched_indices)

    def get_previous_keypoints_descriptors(self):
        """
        Get the last frame's keypoints and descriptors.

        Returns:
            tuple: Keypoints and descriptors of the previous frame.
        """
        return self._kp_prev, self._des_prev
    
    # --- Import and Export Functionality ---
    def export_state(self, file_path: str):
        """
        Saves the current SLAM system's state to a file.

        Args:
            file_path (str): Path to the file where the state will be saved.
        """
        # Convert keypoints to a serializable format (tuples)
        kp_prev_serializable = [(kp.pt, kp.size, kp.angle, kp.response, kp.octave, kp.class_id) for kp in self._kp_prev]
        
        state = {
            'K': self._K,
            'K_inv': self._K_inv,
            'world_points': self._world_points,
            'world_descriptors': self._world_descriptors,
            'R_prev': self._R_prev,
            't_prev': self._t_prev,
            'des_prev': self._des_prev,
            'kp_prev': kp_prev_serializable,
            'scale': self._scale
        }

        with open(file_path, 'wb') as file:
            pickle.dump(state, file)

    def import_state(self, file_path: str):
        """
        Loads a previously saved SLAM system's state from a file.

        Args:
            file_path (str): Path to the file from which the state will be loaded.
        """
        with open(file_path, 'rb') as file:
            state = pickle.load(file)
        
        self._K = state['K']
        self._K_inv = state['K_inv']
        self._world_points = state['world_points']
        self._world_descriptors = state['world_descriptors']
        self._R_prev = state['R_prev']
        self._t_prev = state['t_prev']
        self._des_prev = state['des_prev']
        
        # Convert the tuples back into cv2.KeyPoint objects
        self._kp_prev = [cv2.KeyPoint(x=pt[0][0], y=pt[0][1], size=pt[1], angle=pt[2], 
                                    response=pt[3], octave=pt[4], class_id=pt[5]) 
                        for pt in state['kp_prev']]
        
        self._scale = state['scale']

    def process_frame(self, curr_frame: np.ndarray) -> tuple[np.ndarray, np.ndarray]:
        """
        Processes the current frame to compute the relative rotation and translation.
        Performs the following steps:
            - Detect keypoints and descriptors.
            - Match features between the previous and current frames.
            - Estimate essential matrix and recover pose.
            - Optionally refine the pose using PnP and global map points.
            - Triangulate new points and add them to the global map.

        Args:
            curr_frame (np.ndarray): The current image/frame from the camera.

        Returns:
            tuple: Refined rotation (R) and translation (t) relative to the world frame.
        """
        # Convert frame to grayscale and detect keypoints and descriptors
        gray_frame = cv2.cvtColor(curr_frame, cv2.COLOR_BGR2GRAY)
        kp_curr, des_curr = self._sift.detectAndCompute(gray_frame, None)

        # Handle first frame initialization
        if self._R_prev is None:
            self._R_prev = np.eye(3)
            self._t_prev = np.zeros((3, 1))
            self._kp_prev, self._des_prev = kp_curr, des_curr
            return self._R_prev, self._t_prev

        # Find feature matches between previous and current frames
        matches = self._find_feature_matches(self._des_prev, des_curr)
        pts_prev, pts_curr = self._get_matched_points(self._kp_prev, kp_curr, matches)

        # Estimate the essential matrix using RANSAC and recover pose
        E, mask = cv2.findEssentialMat(pts_prev, pts_curr, self._K, cv2.RANSAC, RANSAC_PROB, RANSAC_THRESHOLD)
        matches = [match for match, accepted in zip(matches, mask) if accepted]
        _, R_rel, t_rel, mask_pose = cv2.recoverPose(E, pts_prev, pts_curr, self._K)

        # Update world pose estimates using relative transformations
        R_world_est = R_rel @ self._R_prev
        t_world_est = (R_rel @ self._t_prev) + t_rel

        # Refine projection matrix with known 3D points using PnP
        P_est, R_world, t_world, global_matches = self._refine_projection_matrix(
            self._K @ np.hstack((R_world_est, t_world_est)), R_world_est, t_world_est, kp_curr, des_curr)

        # Optionally triangulate new 3D points and add them to the global map
        if self._save_new_points:
            self._update_3d_pts(matches, global_matches, kp_curr, des_curr, P_est)

        # Update the previous frame information
        self._R_prev, self._t_prev = R_world, t_world
        self._kp_prev, self._des_prev = kp_curr, des_curr

        return R_world, t_world

    def _find_feature_matches(self, des_prev: np.ndarray, des_curr: np.ndarray) -> list:
        """
        Finds feature matches between two sets of descriptors using the ratio test.

        Args:
            des_prev (np.ndarray): Descriptors from the previous frame.
            des_curr (np.ndarray): Descriptors from the current frame.

        Returns:
            list: Good matches based on the ratio test.
        """
        matches = self._matcher.knnMatch(des_prev, des_curr, k=2)
        good_matches = [m for m, n in matches if m.distance < 0.75 * n.distance]
        return sorted(good_matches, key=lambda x: x.distance)

    def _get_matched_points(self, kp_prev: list, kp_curr: list, matches: list) -> tuple[np.ndarray, np.ndarray]:
        """
        Converts feature matches into 2D point correspondences for the previous and current frames.

        Args:
            kp_prev (list): Keypoints from the previous frame.
            kp_curr (list): Keypoints from the current frame.
            matches (list): List of feature matches.

        Returns:
            tuple: Matched 2D points from the previous and current frames.
        """
        pts_prev = np.array([kp_prev[m.queryIdx].pt for m in matches], dtype=np.float64).reshape(-1, 1, 2)
        pts_curr = np.array([kp_curr[m.trainIdx].pt for m in matches], dtype=np.float64).reshape(-1, 1, 2)
        return pts_prev, pts_curr

    def _refine_projection_matrix(self, P_est: np.ndarray, R_est: np.ndarray, t_est: np.ndarray,
                                  kp_curr: list, des_curr: np.ndarray) -> tuple[np.ndarray, np.ndarray, np.ndarray, list]:
        """
        Refines the estimated projection matrix using PnP with known 3D points and their corresponding 2D matches.

        Args:
            P_est (np.ndarray): Initial estimate of the projection matrix.
            R_est (np.ndarray): Initial estimate of the rotation matrix.
            t_est (np.ndarray): Initial estimate of the translation vector.
            kp_curr (list): Keypoints from the current frame.
            des_curr (np.ndarray): Descriptors from the current frame.

        Returns:
            tuple: Refined projection matrix, rotation matrix, translation vector, and the global matches.
        """
        # Find matches between global 3D points and current frame's 2D features
        global_matches = self._find_feature_matches(self._world_descriptors, des_curr)
        
        # Check if there are enough matches for reliable PnP
        if len(global_matches) < 5:
            # If not enough matches, return the original estimates
            return P_est, R_est, t_est, global_matches
                
        # Extract matched 3D points (from global map) and 2D points (from current frame)
        pts_3d = np.array([self._world_points[m.queryIdx] for m in global_matches])
        pts_2d = np.array([kp_curr[m.trainIdx].pt for m in global_matches])
        try:
            # Solve the PnP problem to get a refined rotation and translation
            success, rvec, t_refined = cv2.solvePnPRansac(
                pts_3d, 
                pts_2d, 
                self._K, 
                rvec=R_est, 
                tvec=t_est, 
                # reprojectionError=RANSAC_THRESHOLD,
                # confidence=RANSAC_PROB,
                useExtrinsicGuess=True, 
                flags=cv2.SOLVEPNP_ITERATIVE)
        except:
            return P_est, R_est, t_est, global_matches
        
        if success:
            # Convert the rotation vector (rvec) to a rotation matrix
            R_refined, _ = cv2.Rodrigues(rvec)
            # Formulate the refined projection matrix
            P_refined = self._K @ np.hstack((R_refined, t_refined))
            return P_refined, R_refined, t_refined, global_matches
        else:
            # If PnP fails, return the original estimates
            return P_est, R_est, t_est, global_matches

    def _recalc_known_3d_pts(self, existing_matches: list, kp_curr: list, des_curr: np.ndarray, P_curr: np.ndarray, P_prev: np.ndarray):
        pass
    
    def _add_new_3d_pts(self, new_matches: list, kp_curr: list, des_curr: np.ndarray, P_curr: np.ndarray, P_prev: np.ndarray):
        if len(new_matches) < 5:
            return

        # Extract 2D points for triangulation
        pts_prev = np.array([self._kp_prev[m.queryIdx].pt for m in new_matches])
        pts_curr = np.array([kp_curr[m.trainIdx].pt for m in new_matches])

        # Triangulate new 3D points using the projection matrices of the previous and current frames
        
        try:
            pts_4d_homogeneous = cv2.triangulatePoints(P_prev, P_curr, pts_prev.T, pts_curr.T)
        except:
            print(P_prev, P_curr, pts_prev.T, pts_curr.T)
            exit(1)

        # print(len(self._world_points))
        
        # Convert the homogeneous 4D points to 3D (by dividing by the last element)
        pts_3d = pts_4d_homogeneous[:3] / pts_4d_homogeneous[3]

        # Append the new points and their descriptors to the global map
        if len(self._world_points) == 0:
            self._world_points = pts_3d.T
        else:
            self._world_points = np.vstack((self._world_points, pts_3d.T))

        if len(self._world_descriptors) == 0:
            self._world_descriptors = des_curr[self._unmatched_indices]
        else:
            self._world_descriptors = np.vstack((self._world_descriptors, des_curr[self._unmatched_indices]))
    
    def _update_3d_pts(self, matches: list, global_matches: list, kp_curr: list, des_curr: np.ndarray, P_curr: np.ndarray):
        """
        Triangulates new 3D points from the current frame and adds them to the global map.
        Ensures that the new points are not already part of the map.

        Args:
            matches (list): Matches between previous and current frames.
            global_matches (list): Matches between the global map and the current frame.
            kp_curr (list): Keypoints from the current frame.
            des_curr (np.ndarray): Descriptors from the current frame.
            P_curr (np.ndarray): Current projection matrix.
        """
        # Determine unmatched and matched keypoints for the current frame
        self._unmatched_indices = np.zeros(len(kp_curr), dtype=bool)
        for match in matches:
            self._unmatched_indices[match.trainIdx] = True
        for global_match in global_matches:
            self._unmatched_indices[global_match.trainIdx] = False
        
        # matches between the last and current frame, that do not refer to known 3d points.
        unmatched = [m for m in matches if self._unmatched_indices[m.trainIdx]]
        
        # matches between the last and current frame, that refer to known 3d points.
        matched = [m for m in matches if not self._unmatched_indices[m.trainIdx]]
        
        P_prev = self._K @ np.hstack((self._R_prev, self._t_prev))

        self._add_new_3d_pts(unmatched, kp_curr, des_curr, P_curr, P_prev)