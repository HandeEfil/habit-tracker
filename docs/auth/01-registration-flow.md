# User Registration — Validation Rules

## Overview

Kullanıcı kayıt işlemi sırasında gönderilen veriler, Bean Validation anotasyonları ile doğrulanır.

Geçersiz alanlar için ilgili validation mesaj anahtarları döndürülür.

## Field Validations

| Field          | Validation    | Description                                                                       | Error Message                                  |
|----------------|---------------|-----------------------------------------------------------------------------------|------------------------------------------------|
| `name`         | `@NotBlank`   | İsim alanı boş, null veya yalnızca whitespace olamaz.                             | `name.blank.error`                             |
| `password`     | `@NotBlank`   | Şifre alanı boş bırakılamaz.                                                      | `password.blank.error`                         |
| `password`     | `@Pattern`    | En az bir küçük harf, bir büyük harf, bir rakam ve bir özel karakter içermelidir. | `password.complexity.error`                    |
| `mail`         | `@Email`      | E-posta adresi formatını doğrular.                                                | `mail.format.error`                            |
| `mail`         | `@NotBlank`   | E-posta alanı boş bırakılamaz.                                                    | `mail.blank.error`                             |
| `mobileNumber` | `@NotBlank`   | Telefon numarası boş bırakılamaz.                                                 | `number.blank.error`                           |
| `mobileNumber` | `@Pattern`    | İsteğe bağlı `+` ile başlayabilir ve 10–15 rakam içermelidir.                     | `number.format.error`                          |
| `birthDate`    | `@NotNull`    | Doğum tarihi zorunludur.                                                          | `birthDate.blank.error`                        |
| `birthDate`    | `@Past`       | Doğum tarihi bugünden önce olmalıdır.                                             | `birthDate.past.error`                         |
| `birthDate`    | `@MinimumAge` | Kullanıcının minimum yaş koşulunu sağlamasını kontrol eder.                       | Annotation'ın kendi konfigürasyonuna bağlıdır. |

## Password Policy

Password validation regex:

`^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).+$`

Şifre aşağıdaki koşulları sağlamalıdır:

- En az bir küçük harf (`a-z`)
- En az bir büyük harf (`A-Z`)
- En az bir rakam (`0-9`)
- En az bir özel karakter (`@ $ ! % * ? & #`)

## Mobile Number Format

Mobile number validation regex:

`^\\+?[0-9]{10,15}$`

Kurallar:

- Numara `+` ile başlayabilir, ancak zorunlu değildir.
- `+` dışında yalnızca rakamlara izin verilir.
- Toplam rakam sayısı 10 ile 15 arasında olmalıdır.
- Boşluk, tire ve parantez gibi karakterler kabul edilmez.

Examples:

| Value               | Result  |
|---------------------|---------|
| `+905551234567`     | Valid   |
| `905551234567`      | Valid   |
| `5551234567`        | Valid   |
| `+90 555 123 45 67` | Invalid |
| `0555-123-45-67`    | Invalid |

## Birth Date Validation

Doğum tarihi üç ayrı kontrolle doğrulanır:

1. `@NotNull`: Alanın gönderilmesi zorunludur.
2. `@Past`: Tarih bugünden önce olmalıdır.
3. `@MinimumAge`: Projede tanımlanan minimum yaş koşulu sağlanmalıdır.

`@MinimumAge` özel bir validation anotasyonudur. Minimum yaş değeri ve hesaplama mantığı ilgili validator
implementasyonunda tanımlanır.

## Validation Summary

Kayıt isteği, alanların tüm validation kurallarını sağlaması durumunda sonraki iş mantığına aktarılır.

Validation kuralları kayıt DTO'su üzerinde uygulanır; kullanıcı kaydı, veritabanı işlemleri ve diğer iş kuralları ilgili
servis katmanında yürütülür.

## Register Flow

- Gelen mail e ait kullanıcı var mı diye bakılır varsa hata atar.
- Kullanıcının şifresi encode edilir.
- Kaydedilir. UserDto döner. gelen request in büyük ölçüde benzeri şifre vb hariç.

## TODO

Notification modülü tamamlandıktan sonra:

- [ ] Register akışına email verification eklenecek.
    - [ ] 6 haneli doğrulama kodu oluşturulacak ve e-posta ile gönderilecek.
    - [ ] E-posta adresinin sistemde kayıtlı olup olmadığı son kullanıcıya gösterilmeyecek.
    - [ ] Doğrulama kodunun geçerlilik süresi ve tek kullanımlık olması sağlanacak.