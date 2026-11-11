# Chạy prototype VoltHub nối Java API

Bản UI sử dụng: `frontend`.

## Khởi chạy (hai cửa sổ PowerShell)

Tại thư mục gốc dự án:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-api.ps1
```

```powershell
cd .\frontend
npm install
npm run dev
```

Mở http://localhost:8443. Backend nghe tại 127.0.0.1:8080; Vite chuyển tiếp `/api` nên không cần cấu hình CORS. Cần Node hỗ trợ Vite 8 và JDK 21 (`javac` trên PATH). Script API tự chọn `java.exe` cùng thư mục với `javac`, tránh máy đang ưu tiên Java 8.

## Luồng demo

1. Trang chủ: tìm theo ID/SKU/tên, lọc danh mục, hãng, giá; mở chi tiết.
2. Thêm giỏ, đổi số lượng, xóa; dữ liệu lưu CSDL.
3. Thanh toán: nhập địa chỉ, chọn tiêu chuẩn (25.000đ) hoặc nhanh (49.000đ), chọn COD/chuyển khoản, xác nhận. Backend chốt giá, phí, trừ kho và xóa giỏ trong transaction.
4. Theo dõi: chọn đơn của khách hoặc nhập ID dạng số; đọc lịch sử và mã vận đơn thật trong CSDL.
5. Tài khoản → Mở trang quản trị: thêm/sửa/xóa/tìm linh kiện; Đơn hàng hoặc Vận chuyển → Đóng gói → Bàn giao (nhập hãng và mã vận đơn) → Đã giao. Hủy khi chờ xác nhận/đóng gói sẽ hoàn kho.
6. Quay lại Theo dõi để xem thay đổi. Tải lại trang vẫn đọc được đơn đã lưu.

Đây là prototype cục bộ: khách demo ID 1, admin ID 4, chưa có đăng nhập/phân quyền HTTP. Chuyển khoản chỉ ghi nhận phương thức; không tích hợp thanh toán ngân hàng. Địa chỉ mặc định lưu trong trình duyệt. API dùng service/DAO hiện có và cấu hình `src/main/resources/db.properties` (mặc định SQLite). Thống kê doanh thu và ví điện tử không thuộc phạm vi tích hợp.

## API

GET `/api/products`, `/api/categories`, `/api/cart`, `/api/orders`, `/api/orders?admin=true`, `/api/tracking?id=1`.

POST `/api/products`, `/api/cart`, `/api/orders`, `/api/status`, `/api/shipping`; DELETE `/api/products`. Request body dùng `application/x-www-form-urlencoded`. Xem `src/App.tsx` và `ApiServer.java` để đối chiếu trường dữ liệu. Lỗi nghiệp vụ trả HTTP 400 kèm `message`.

## Kiểm tra

- `npm run build` và `npx tsc --noEmit` trong thư mục UI.
- `node api-smoke.cjs` tại thư mục gốc: kiểm tra CRUD, giỏ, đặt hàng/phí, chống trùng, trừ/hoàn kho, vận chuyển/lịch sử. Dùng CSDL tạm riêng; dùng cổng test 18080, độc lập backend demo 8080. Runtime được tìm từ `javac` trên PATH.
- Bộ kiểm thử Java hiện có: 54 kiểm tra. Dùng JDK 21 cho cả biên dịch và thực thi.

QR ACB hiển thị khi chọn chuyển khoản và trong chi tiết đơn. QR tĩnh; nhập số tiền và nội dung VOLTHUB theo mã đơn. Xác nhận thanh toán thủ công.
