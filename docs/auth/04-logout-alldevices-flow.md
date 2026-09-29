# Logout All Devices

## Overview

Logout All Devices işlemi, kullanıcının tüm cihazlardaki oturumlarını sonlandırmak amacıyla gerçekleştirilir.

Endpoint yalnızca `CUSTOMER` rolüne sahip kullanıcılar tarafından çağrılabilir.

## Endpoint

| Property        | Value                 |
|-----------------|-----------------------|
| HTTP Method     | `POST`                |
| Endpoint        | `/logout-all-devices` |
| Authorization   | `CUSTOMER`            |
| Response Status | `200 OK`              |

## Request

### Authentication

İstek, kimliği doğrulanmış kullanıcı tarafından gerçekleştirilmelidir.

Kullanıcı bilgisi Spring Security `Authentication` nesnesi üzerinden alınır.

- `authentication.getName()` kullanılarak kullanıcının kimliği elde edilir.
- Kullanıcı kimliği logout servisine iletilir.

## Logout Flow

- Email'e ait olan tüm hesaplardaki refresh tokenlar revoke a çekilir.
- Kullanıcı blacklistlenir logout süresinden önce oluşturulmuş access tokenlara izin verilmez.
- JwtAuthenticationFilter de blacklistlenme süresiyle beraber kontrol edilir.

## Response

İşlem başarılı olduğunda `200 OK` status kodu döndürülür.

Response body bulunmaz.

