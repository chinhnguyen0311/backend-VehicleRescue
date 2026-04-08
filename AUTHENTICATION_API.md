# Vehicle Rescue Authentication API - Quick Reference

## Base URL
```
http://localhost:8080
```

## 1. Register a New Account

**Endpoint:** `POST /auth/register`

**Request:**
```json
{
  "username": "mechanic_name",
  "email": "mechanic@example.com",
  "password": "password123",
  "fullName": "Nguyễn Văn Mechanic",
  "phoneNumber": "0901234567"
}
```

**Validation Rules:**
- Username: 3-50 characters, must be unique
- Email: Valid email format, must be unique
- Password: Minimum 6 characters
- fullName: Required, up to 255 characters
- phoneNumber: Optional

**Success Response (201 Created):**
```json
"Đăng ký thành công!"
```

**Error Response (409 Conflict):**
```json
{
  "code": 1003,
  "message": "Tên đăng nhập đã được sử dụng",
  "status": 409
}
```

---

## 2. Login

**Endpoint:** `POST /auth/login`

**Request:**
```json
{
  "username": "mechanic_name",
  "password": "password123"
}
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "account": {
    "accountId": "123e4567-e89b-12d3-a456-426614174000",
    "username": "mechanic_name",
    "email": "mechanic@example.com",
    "fullName": "Nguyễn Văn Mechanic",
    "avatarUrl": null,
    "phoneNumber": "0901234567",
    "role": "MECHANIC",
    "isActive": true,
    "emailVerified": false
  }
}
```

**Response Headers:**
```
Set-Cookie: refresh_token={refreshToken}; HttpOnly; Secure; Path=/; Max-Age=86400
```

**Error Response (401 Unauthorized):**
```json
{
  "code": 1002,
  "message": "Mật khẩu không chính xác",
  "status": 401
}
```

---

## 3. Get Current Account Info

**Endpoint:** `GET /auth/account`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Success Response (200 OK):**
```json
{
  "accountId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "mechanic_name",
  "email": "mechanic@example.com",
  "fullName": "Nguyễn Văn Mechanic",
  "avatarUrl": null,
  "phoneNumber": "0901234567",
  "role": "MECHANIC",
  "isActive": true,
  "emailVerified": false
}
```

**Error Response (401 Unauthorized):**
```json
{
  "code": 1009,
  "message": "Access token không hợp lệ",
  "status": 401
}
```

---

## 4. Refresh Access Token

**Endpoint:** `GET /auth/refresh`

**Option A - Using Cookie (Web):**
```
GET /auth/refresh
```
Automatically uses `refresh_token` cookie if present.

**Option B - Using Header (Mobile):**
```
GET /auth/refresh
X-Refresh-Token: {refreshToken}
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "account": {
    "accountId": "123e4567-e89b-12d3-a456-426614174000",
    ...
  }
}
```

**Error Response (401 Unauthorized):**
```json
{
  "code": 1007,
  "message": "Refresh token không hợp lệ hoặc đã bị thu hồi",
  "status": 401
}
```

---

## 5. Logout

**Endpoint:** `POST /auth/logout`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Success Response (200 OK):**
```json
"Đăng xuất thành công"
```

**Response Headers:**
```
Set-Cookie: refresh_token=null; HttpOnly; Secure; Path=/; Max-Age=0
```

**Error Response (401 Unauthorized):**
```json
{
  "code": 1009,
  "message": "Access token không hợp lệ",
  "status": 401
}
```

---

## 6. Change Password

**Endpoint:** `POST /auth/change-password`

**Headers:**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request:**
```json
{
  "currentPassword": "password123",
  "newPassword": "newpassword456",
  "confirmPassword": "newpassword456"
}
```

**Validation Rules:**
- currentPassword: Must match current password (required)
- newPassword: Minimum 6 characters (required)
- confirmPassword: Must match newPassword (required)

**Success Response (200 OK):**
```json
"Đổi mật khẩu thành công"
```

**Error Response (400 Bad Request):**
```json
{
  "code": 1011,
  "message": "Mật khẩu không khớp",
  "status": 400
}
```

**Error Response (401 Unauthorized):**
```json
{
  "code": 1002,
  "message": "Mật khẩu không chính xác",
  "status": 401
}
```

---

## Error Codes Reference

| Code | Message | HTTP Status |
|------|---------|-------------|
| 1001 | Tài khoản không tồn tại | 404 |
| 1002 | Mật khẩu không chính xác | 401 |
| 1003 | Tên đăng nhập đã được sử dụng | 409 |
| 1004 | Email đã được đăng ký | 409 |
| 1005 | Mật khẩu hiện tại không đúng | 400 |
| 1006 | Phiên đăng nhập không hợp lệ hoặc đã hết hạn | 401 |
| 1007 | Refresh token không hợp lệ hoặc đã bị thu hồi | 401 |
| 1008 | Tài khoản không có quyền truy cập | 403 |
| 1009 | Access token không hợp lệ | 401 |
| 1010 | Tài khoản không hoạt động | 403 |
| 1011 | Mật khẩu không khớp | 400 |
| 9999 | Lỗi hệ thống | 500 |

---

## Token Information

### Access Token
- **Duration**: 3600 seconds (1 hour)
- **Usage**: Include in `Authorization: Bearer {token}` header
- **Contains**: 
  - User info (accountId, username, email, etc.)
  - Role information
  - Issue and expiration time

### Refresh Token
- **Duration**: 86400 seconds (24 hours)
- **Storage**: 
  - HTTP-only cookie (Web)
  - Header or local storage (Mobile)
- **Usage**: Used to obtain new access tokens when they expire
- **Stored in DB**: Yes, for validation

---

## Example Client Implementation (JavaScript)

```javascript
// Store tokens
const storeTokens = (response) => {
  localStorage.setItem('accessToken', response.accessToken);
  localStorage.setItem('refreshToken', response.refreshToken);
};

// Get authorization header
const getAuthHeader = () => {
  const token = localStorage.getItem('accessToken');
  return {
    'Authorization': `Bearer ${token}`
  };
};

// Refresh token when expired
const refreshAccessToken = async () => {
  const response = await fetch('http://localhost:8080/auth/refresh', {
    method: 'GET',
    headers: {
      'X-Refresh-Token': localStorage.getItem('refreshToken')
    }
  });
  const data = await response.json();
  storeTokens(data);
  return data.accessToken;
};

// Logout
const logout = async () => {
  await fetch('http://localhost:8080/auth/logout', {
    method: 'POST',
    headers: getAuthHeader()
  });
  localStorage.clear();
};
```

---

## Account Roles

- **MECHANIC**: Default role for service providers
  - Can access rescue services
  - Can view and manage their own requests

- **ADMIN**: Administrative access
  - Access to `/api/admin/**` endpoints
  - Full system management capabilities

---

## Security Notes

⚠️ **HTTPS Required in Production**: All token transmission should use HTTPS
⚠️ **Token Expiration**: Access tokens expire after 1 hour
⚠️ **Refresh Token Security**: Refresh tokens are HTTP-only and secure
⚠️ **CORS**: Currently allows localhost (3000, 3001, 8080) - configure for production
⚠️ **Password Requirements**: Minimum 6 characters (consider stronger requirements in production)

---

## Testing Commands

### Register
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_mechanic",
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test Mechanic",
    "phoneNumber": "0901234567"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_mechanic",
    "password": "password123"
  }'
```

### Get Account Info
```bash
curl -X GET http://localhost:8080/auth/account \
  -H "Authorization: Bearer {accessToken}"
```

### Refresh Token
```bash
curl -X GET http://localhost:8080/auth/refresh \
  -H "X-Refresh-Token: {refreshToken}"
```

### Logout
```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer {accessToken}"
```

### Change Password
```bash
curl -X POST http://localhost:8080/auth/change-password \
  -H "Authorization: Bearer {accessToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "password123",
    "newPassword": "newpassword456",
    "confirmPassword": "newpassword456"
  }'
```

