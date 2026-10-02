# Chat & Notification Service

## Vai trò trong hệ thống

Một microservice trong backend (API Gateway → Auth, Marketplace cycle, Audit-log, **Chat & Notification**, AI virtual try-on). Giao tiếp với các service khác qua gRPC, có PostgreSQL riêng. Redis và Kafka (pub/sub) là hạ tầng dùng chung toàn hệ thống.

Nguồn tham chiếu trong tài liệu dự án: nhóm chức năng **G. CHAT & THÔNG BÁO** (F-093 → F-099), use case **UC-061 Real-time Chat** và **UC-062 Receive Notifications**.

## Phạm vi chức năng

| Code | Chức năng | Actor |
| --- | --- | --- |
| F-093 | Chat với tổ chức (Member ↔ Organization) | Member, Organization |
| F-094 | Chat với hộp thư chung của kho (Member ↔ Moderator) | Member |
| F-095 | Trả lời hộp thư kho | Moderator |
| F-096 | Nhận thông báo real-time | Member |
| F-097 | Nhận thông báo real-time | Organization |
| F-098 | Nhận thông báo real-time | Moderator |
| F-099 | Báo Admin việc quá hạn trong hàng đợi | System → Admin |

## Business rules quan trọng

**Chat (F-093, F-094, F-095):**
- **Không có chat Member – Member.** Chỉ 2 kênh: Member–Organization và Member–Kho.
- Kênh Member–Kho là **hộp thư chung của kho** (không phải chat 1-1 với từng Moderator): mọi Moderator thuộc kho đó đều thấy và trả lời được; mỗi tin trả lời hiển thị tên Moderator đã trả lời.
- 3 điểm vào của chat với kho:
  1. Từ trang sản phẩm → vào hộp thư của kho đang giữ sản phẩm đó, kèm thẻ sản phẩm
  2. Từ form ký gửi → vào hộp thư của kho đã chọn trong form
  3. Nhắn chung (không qua context cụ thể) → Member tự chọn kho, mặc định gợi ý kho gần nhất
- Hỗ trợ gửi ảnh, không chỉ text.
- **Yêu cầu độ trễ: dưới 1 giây** — đây là ràng buộc kỹ thuật cứng, quyết định việc chọn WebSocket thay vì polling.

**Notification (F-096, F-097, F-098, F-099):**
- Mỗi actor nhận một tập trigger khác nhau — đây là nguồn khi thiết kế enum `NotificationType`:
  - **Member**: tin nhắn mới; kết quả duyệt form ký gửi, form sắp/đã hết hạn; biên nhận chờ xác nhận; sản phẩm đã lên sàn/đã bán; nhắc sắp hết kỳ ký gửi; yêu cầu rút hàng; trạng thái đơn hàng; kết quả trả hàng/hoàn tiền; kết quả rút tiền; kết quả xác minh tài khoản ngân hàng; hàng bị xác định giả; đăng ký quyên góp đã nhận/sắp-đã hết hạn; bài viết mới từ tổ chức đã theo dõi; kết quả báo cáo vi phạm
  - **Organization**: tin nhắn mới; đăng ký quyên góp mới; kết quả xác minh tổ chức; kết quả duyệt đổi thông tin pháp lý; chiến dịch bị Moderator ẩn/đóng; nội dung bị gỡ do vi phạm (kèm lý do)
  - **Moderator**: theo kho (form ký gửi mới, đơn cần đóng gói, yêu cầu trả hàng, hàng trả đã về, đơn chờ hoàn tiền, yêu cầu rút tiền, tin nhắn hộp thư kho) + toàn hệ thống (hồ sơ tổ chức mới, yêu cầu đổi thông tin pháp lý, báo cáo vi phạm mới) + cảnh báo việc đã claim sắp tự nhả (ngưỡng 48h)
  - **Admin**: việc bị quá hạn / tự nhả nhiều lần trong hàng đợi (F-099, dùng để theo dõi vận hành)
- Tất cả đều yêu cầu **real-time**, không phải batch/polling.

## Nguồn phát sinh event (tích hợp với service khác)

Service này **không tự sinh ra phần lớn trigger** — nó nhận event từ các service nghiệp vụ khác qua **Kafka** (Auth, Marketplace cycle...) rồi convert thành notification/chat message tương ứng. Đây là ranh giới trách nhiệm chính cần thiết kế rõ: service nguồn publish event khi trạng thái đổi (ví dụ đơn hàng chuyển trạng thái, form ký gửi được duyệt), service này subscribe và xử lý fan-out tới đúng actor.

## Gợi ý data model (theo tài liệu tổng quan dự án)

- `conversation`: `type` (MEMBER_ORG / MEMBER_WAREHOUSE), `warehouse_id`, `sender`, `content`, `image`, `replied_by_staff`
- `notification`: thuộc nhóm bảng hệ thống dùng chung, cần ít nhất `recipient_id`, `recipient_type`, `type`, `content`, `read_at`, `created_at`, và liên kết tới entity gốc (order_id, consignment_id...) để Member bấm vào xem chi tiết

## Kiến trúc kỹ thuật của riêng service này

- **Tự xây (in-house)**: business logic, lưu trữ dữ liệu chat/notification, delivery real-time qua WebSocket
- **Dùng kênh ngoài**: Firebase Cloud Messaging (push), SMTP provider (email)
- Đóng gói Docker, expose metrics cho Prometheus (Spring Boot Actuator)

## Tech stack

- Spring Boot 4.1.1, Java 21, package `com.rehome.notification` (module trong Maven multi-module repo, kế thừa parent `pom.xml` root)

## Quy ước code

- Code đơn giản, sạch, không dùng comment trang trí kiểu `// ───────────`
- Comment tự nhiên, chỉ khi logic không tự giải thích được
- Thực dụng, tránh over-engineering (ví dụ: chưa cần abstraction layer cho WebSocket nếu chưa có nhu cầu đổi provider)
