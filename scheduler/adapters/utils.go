package adapters

import "context"

// extractUserIDFromContext extracts user ID from context
func extractUserIDFromContext(ctx context.Context) string {
	if userID := ctx.Value("userID"); userID != nil {
		if id, ok := userID.(string); ok {
			return id
		}
	}
	return "default-user"
}

// getUserIDFromContext extracts user ID from JWT authentication context
func getUserIDFromContext(ctx context.Context) string {
	if userID, ok := ctx.Value("user_id").(string); ok {
		return userID
	}
	if claims, ok := ctx.Value("jwt_claims").(map[string]interface{}); ok {
		if userID, ok := claims["sub"].(string); ok {
			return userID
		}
		if userID, ok := claims["user_id"].(string); ok {
			return userID
		}
	}
	return ""
}
