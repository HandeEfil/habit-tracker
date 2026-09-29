# User Logout

## Overview

Logout işlemi, kullanıcının mevcut oturumunu sonlandırmak ve ilgili token'ların tekrar kullanılmasını engellemek
amacıyla gerçekleştirilir.

Logout endpoint'i yalnızca `CUSTOMER` rolüne sahip kullanıcılar tarafından çağrılabilir.

## Endpoint

| Property        | Value      |
|-----------------|------------|
| HTTP Method     | `POST`     |
| Endpoint        | `/logout`  |
| Authorization   | `CUSTOMER` |
| Response Status | `200 OK`   |

## Request

Logout isteğinde aşağıdaki bilgiler gönderilir:

| Parameter        | Source               | Description                                                    |
|------------------|----------------------|----------------------------------------------------------------|
| `requestDTO`     | Request Body         | Refresh token bilgisini içerir.                                |
| `accessToken`    | Authorization Header | Mevcut access token'ı içerir.                                  |
| `authentication` | Spring Security      | Kimliği doğrulanmış kullanıcıya ait authentication bilgisidir. |

### Request Body

`RefreshTokenRequestDTO` kullanılır.

### FLOW

- JWT Service ten session Id alınır.
- Session id ve mail ile session revoke edilir.
- Access token blacklistlenir. access token süresince redis te tutulur.

```json
{
  "refreshToken": "..."
}