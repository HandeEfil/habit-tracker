# Change Password

## Overview

Change Password işlemi, kimliği doğrulanmış kullanıcının mevcut şifresini yeni bir şifre ile değiştirmesini sağlar.

Endpoint yalnızca `CUSTOMER` rolüne sahip kullanıcılar tarafından çağrılabilir.

## Endpoint

| Property        | Value              |
|-----------------|--------------------|
| HTTP Method     | `POST`             |
| Endpoint        | `/change-password` |
| Authorization   | `CUSTOMER`         |
| Response Status | `200 OK`           |

## Request

### Request Body

Şifre değiştirme isteğinde `ChangePasswordDTO` kullanılır.

| Field         | Type     | Description                                  |
|---------------|----------|----------------------------------------------|
| `oldPassword` | `String` | Kullanıcının mevcut şifresi.                 |
| `newPassword` | `String` | Kullanıcının belirlemek istediği yeni şifre. |

Example:

    {
      "oldPassword": "OldPassword123!",
      "newPassword": "NewPassword123!"
    }

### Authentication

- Kullanıcının kimliği Spring Security `Authentication` nesnesi üzerinden alınır.
- `authentication.getName()` kullanılarak kullanıcının e-posta adresi elde edilir.
- Mevcut access token, Authorization Header üzerinden alınır.
- Access token ve kullanıcı bilgileri servis katmanına iletilir.

## Change Password Flow

- Authentication dan gelen mail db de aranır. Bulunmazsa 401 atar.
- Session id çıkartılır.
- Eğer şifre uyuşmazsa 401 verir. Şifre değişemedi diye audit modülü ile loglama yapılır.
- Uyuşursa:
    - Çekilen session id hariç diğer refresh token ları revoke ediyoruz.
    - Yeni şifre encode edilip kaydedilir.
    - Audit ile loglanır.

## Response

İşlem sonucunda `200 OK` status kodu döndürülür.

Response body, `loginRegisterService.changePassword()` metodunun dönüş tipine göre belirlenir.
