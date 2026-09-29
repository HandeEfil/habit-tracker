## Close Account

### Endpoint

- **Method:** `POST`
- **Path:** `/close-account`
- **Authorization:** `CUSTOMER` role required via `@PreAuthorize`.

### Request

- **DTO:** `CloseAccountDTO`
- **Validation:** `@Valid`
- **Required field:** `password` (used to verify the user's password).

### Flow

- Kullanıcı aranır şifre eşleşmesi yapılır uyuşma yoksa hata atar 401.
- Race Condition'ı önlemek adına kullanıcının enabled ı false a çekilir.
- Refresh token'lar revoke edilir.
- Access token'ları blacklist e atılır.

### Response

- **Status:** `200 OK`
- **Body:** Empty (`null`)

### Notes

- Only authenticated users with the `CUSTOMER` role can access this endpoint.
- The account closure logic is handled by `loginRegisterService.closeAccount()`.
- The controller does not directly handle account deletion, token revocation, or audit operations; these depend on the
  service implementation.