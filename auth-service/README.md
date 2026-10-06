# JWT Demo - Spring Boot + Spring Security + JWT + Refresh Token

Project mẫu Spring Boot 3 minh hoạ xác thực bằng JWT (access token) kết hợp refresh token,
dùng Spring Security 6, Spring Data JPA, thư viện `jjwt` và PostgreSQL (H2 dùng cho test).

Đã tích hợp OTP email cho đăng ký và quên mật khẩu. Xem [hướng dẫn cấu hình SMTP và API OTP](OTP.md).

## Cấu trúc chính

```
src/main/java/com/example/jwtdemo/
├── config/SecurityConfig.java          # Cấu hình Spring Security, CORS, filter chain
├── controller/
│   ├── AuthController.java             # /api/auth/register, login, refresh, logout
│   └── TestController.java             # endpoint mẫu để test phân quyền
├── dto/
│   ├── request/                         # Dữ liệu nhận từ client
│   └── response/                        # Dữ liệu trả về, gồm cả ApiError
├── entity/
│   ├── User.java                        # implements UserDetails
│   ├── Role.java                        # ROLE_USER, ROLE_ADMIN
│   └── RefreshToken.java                # refresh token lưu DB (UUID), có thể revoke
├── repository/                          # Spring Data JPA repository
├── security/
│   ├── JwtService.java                  # sinh/verify access token (JWT)
│   ├── JwtAuthenticationFilter.java     # filter đọc header Authorization
│   └── UserDetailsServiceImpl.java
├── service/
│   ├── AuthService.java                 # logic đăng ký/đăng nhập/refresh/logout
│   └── RefreshTokenService.java         # quản lý vòng đời refresh token trong DB
└── exception/                           # xử lý lỗi tập trung (@RestControllerAdvice)
```

## Cách hoạt động

- **Access token (JWT)**: hết hạn nhanh (mặc định 15 phút), chứa email + roles, ký bằng HS256.
  Gửi kèm trong header `Authorization: Bearer <token>` cho các API cần bảo vệ.
- **Refresh token**: KHÔNG phải JWT, mà là UUID ngẫu nhiên lưu trong bảng `refresh_tokens`
  cùng với thời hạn (mặc định 7 ngày) và cờ `revoked`. Nhờ vậy có thể thu hồi bất cứ lúc nào
  (ví dụ khi logout, đổi mật khẩu, hoặc phát hiện token bị lộ) mà không cần đợi JWT tự hết hạn.
- **Token rotation**: mỗi lần gọi `/api/auth/refresh`, refresh token cũ bị xoá và cấp token mới,
  giảm rủi ro nếu refresh token bị đánh cắp và dùng lại (replay).

## Chạy project

Yêu cầu: JDK 17+ (test được cả JDK 21), Maven 3.8+, Docker + Docker Compose (để chạy PostgreSQL).

### 1. Bật database PostgreSQL bằng Docker

```bash
docker compose up -d
```

Lệnh này khởi động:
- **PostgreSQL** ở cổng `5432` (database `jwtdemo`, user `jwtdemo`, password `jwtdemo`)
- **pgAdmin** (tuỳ chọn, giao diện quản lý DB qua web) ở `http://localhost:5050`
  (đăng nhập bằng `admin@example.com` / `admin`, add server trỏ tới host `postgres`, port `5432`)

Kiểm tra container đã chạy: `docker compose ps`. Dừng: `docker compose down` (thêm `-v` nếu muốn xoá luôn data).

### 2. Chạy ứng dụng Spring Boot

```bash
mvn spring-boot:run
```

Ứng dụng đọc thông tin kết nối DB từ biến môi trường (có giá trị mặc định khớp với `docker-compose.yml`
ở trên nên không cần cấu hình gì thêm để chạy local):

| Biến môi trường | Mặc định | Ý nghĩa |
|---|---|---|
| `DB_HOST` | `localhost` | Host PostgreSQL |
| `DB_PORT` | `5432` | Port PostgreSQL |
| `DB_NAME` | `jwtdemo` | Tên database |
| `DB_USERNAME` | `jwtdemo` | User DB |
| `DB_PASSWORD` | `jwtdemo` | Password DB |

Ứng dụng chạy ở `http://localhost:8080`.

Chạy `mvn test` để kiểm tra luồng OTP với H2 và email giả lập.

## Swagger UI

Sau khi ứng dụng chạy, mở trình duyệt:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

Cách test API cần xác thực trên Swagger UI:
1. Đăng ký và xác minh OTP theo [OTP.md](OTP.md), rồi gọi `POST /api/auth/login` để lấy `accessToken`.
2. Bấm nút **Authorize** (hình ổ khoá, góc trên bên phải).
3. Dán **chỉ accessToken** vào ô value (không cần gõ chữ `Bearer ` phía trước, Swagger tự thêm).
4. Bấm **Authorize** → **Close**. Từ giờ mọi request gọi từ Swagger UI sẽ tự đính kèm header `Authorization`.

## Các API

### 1. Đăng ký
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"123456"}'
```

### 2. Đăng nhập
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"123456"}'
```
Response:
```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "tokenType": "Bearer"
}
```

### 3. Gọi API cần xác thực
```bash
curl http://localhost:8080/api/user/me \
  -H "Authorization: Bearer eyJhbGciOi..."
```

### 4. Làm mới access token bằng refresh token
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"3fa85f64-5717-4562-b3fc-2c963f66afa6"}'
```

### 5. Đăng xuất (thu hồi toàn bộ refresh token của user)
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOi..."
```

### Endpoint public / phân quyền theo role
- `GET /api/public/hello` — không cần token
- `GET /api/user/me` — cần token hợp lệ (bất kỳ role nào)
- `GET /api/admin/dashboard` — chỉ user có `ROLE_ADMIN`

## Đổi sang MySQL (nếu cần)

Project hiện dùng PostgreSQL mặc định. Muốn đổi sang MySQL, thay phần `spring.datasource`
trong `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jwtdemo?useSSL=false&serverTimezone=UTC
    username: root
    password: yourpassword
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

Và thay dependency `postgresql` trong `pom.xml` bằng:
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Lưu ý bảo mật khi triển khai thật

1. **Không hardcode secret** trong `application.yml` khi lên production — dùng biến môi trường
   (ví dụ `${JWT_SECRET}`) hoặc secret manager (Vault, AWS Secrets Manager...).
2. Bật HTTPS, giới hạn CORS về đúng domain frontend thay vì `*`.
3. Cân nhắc lưu refresh token dưới dạng hash (giống password) thay vì plain text trong DB.
4. Thêm cơ chế giới hạn số lần đăng nhập sai (rate limiting / account lockout) để chống brute-force.
5. Nếu chạy nhiều instance, có thể thay in-memory H2 bằng DB thật + Redis để lưu blacklist token khi cần thu hồi access token ngay lập tức.
