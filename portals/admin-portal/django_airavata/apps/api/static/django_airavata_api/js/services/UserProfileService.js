import UserProfile from "../models/UserProfile";
import FetchUtils from "../utils/FetchUtils";

class UserProfileService {
  list(data = null) {
    if (data) {
      return Promise.resolve(data.map((result) => new UserProfile(result)));
    } else {
      return FetchUtils.get("/api/user-profiles/").then((results) =>
        results.map((result) => new UserProfile(result))
      );
    }
  }
}

// Export as a singleton
export default new UserProfileService();
