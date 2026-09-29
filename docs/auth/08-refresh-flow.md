## Refresh Token

### Endpoint

- **Method:** `POST`
- **Path:** `/refresh`
- **Authentication:** Not explicitly required at controller level.

### Request

- **DTO:** `RefreshTokenRequestDTO`
- **Validation:** `@Valid`
- **Required field:** `refreshToken`

### Flow

- Gelen DTO daki token hashlenir.
- Hashlenen token RefreshToken tablosunda aranır.
- Token'ın expire kontrolü yapılır. -> Bunu db de yapmak gerekli ileride.
- Race condition'ı önlemek için önce token ı revoke etmeyi dener. Güncellenen veri yoksa kullanıldı der ve o zinciri
  şüpheli ilan edip o zincirdeki aktif token ı revoke etti.
- Güncelleme varsa
    - Yeni token generate edilir, hashlenir.
    - Eski token revoke edilir.
    - Eski token a yeni token bağlanır zinciri bulabilmek için.
    - Hem refresh hem access token üretilip raw ları ön yüze gönderilir.

### Response

- **Status:** `200 OK`
- **Body:** `TokenPairResponseDTO`
    - Access token
    - Refresh token

## TODO

- Bu metodda da audit kullanılmalıdır. 