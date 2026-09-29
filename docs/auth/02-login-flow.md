# User Login — Validation Rules

## Overview

Login isteğinde gönderilen e-posta ve şifre alanları Bean Validation anotasyonları ile doğrulanır.

## Field Validations

| Field      | Validation  | Description                    | Error Message          |
|------------|-------------|--------------------------------|------------------------|
| `mail`     | `@Email`    | E-posta formatını doğrular.    | `mail.format.error`    |
| `mail`     | `@NotBlank` | E-posta alanı boş bırakılamaz. | `mail.blank.error`     |
| `password` | `@NotBlank` | Şifre alanı boş bırakılamaz.   | `password.blank.error` |

## Login Flow

1. Gelen e-posta adresine ait kullanıcı aranır.
2. Kullanıcı bulunamazsa login başarısız olur ve `401 Unauthorized` döndürülür.
3. Kullanıcının hesabı kilitli veya dondurulmuşsa login başarısız olur ve `401 Unauthorized` döndürülür.
4. Kullanıcının şifresi doğrulanır.
5. Şifre yanlışsa:
    - `401 Unauthorized` döndürülür.
    - Başarısız giriş kaydı Redis üzerinde tutulur.
    - Hesap zaten kilitliyse başarısız giriş sayacı artırılmaz.
    - Başarısız giriş sayısı, properties dosyasında tanımlanan limiti aşarsa hesap kilitlenir.
    - Hesabın kilitlenmesi Audit modülü üzerinden loglanır.
6. Kimlik doğrulama başarılıysa başarısız login girişimlerine ait sayaç sıfırlanır.
7. Refresh token oluşturulur ve raw refresh token response içerisinde döndürülür.
8. Login response içerisinde aşağıdaki bilgiler yer alır:
    - E-posta adresi
    - JWT access token
    - Refresh token

## JWT Token Structure

JWT access token aşağıdaki bilgileri içerir:

| Claim                   | Description                                                                                               |
|-------------------------|-----------------------------------------------------------------------------------------------------------|
| `sub` (Subject)         | Kullanıcının e-posta adresini içerir.                                                                     |
| `iat` (Issued At)       | Token'ın oluşturulma tarihini belirtir.                                                                   |
| `exp` (Expiration Time) | Token'ın geçerlilik süresini belirtir. Süre properties dosyasında tanımlanır.                             |
| `jti` (JWT ID)          | UUID değeridir. Logout sonrasında access token'ın geçersiz kılınması için Redis üzerinden kontrol edilir. |
| `authorities`           | Kullanıcının yetkilerini içerir.                                                                          |
| `sessionId`             | Session bazlı logout işlemlerinde kullanılır.                                                             |

## TODO

- [ ] JWT Subject değerinin e-posta yerine kullanıcı UID'si olarak kullanılması değerlendirilecek.