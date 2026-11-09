# TÀI LIỆU HƯỚNG DẪN DỰ ÁN & QUY CHUẨN PHÁT TRIỂN TIẾP CODE
## HỆ THỐNG QUẢN LÝ BÁN LINH KIỆN ĐIỆN TỬ TRỰC TUYẾN (DỰ ÁN PTTKYC)

> **Dành cho các thành viên trong nhóm phát triển**: Tài liệu này giải thích toàn bộ kiến trúc mã nguồn hiện tại, quy ước lập trình, cách thức vận hành và hướng dẫn từng bước chi tiết để các thành viên tiếp tục viết tiếp các chức năng còn lại theo đúng thiết kế của môn học.

---

## MỤC LỤC
1. [Nguồn tài liệu gốc & Base thiết kế](#1-nguồn-tài-liệu-gốc--base-thiết-kế)
2. [Quy ước lập trình bắt buộc (Coding Conventions)](#2-quy-ước-lập-trình-bắt-buộc-coding-conventions)
3. [Kiến trúc phân tầng & Luồng dữ liệu (Architecture & Data Flow)](#3-kiến-trúc-phân-tầng--luồng-dữ-liệu-architecture--data-flow)
4. [Các chức năng đã hoàn thành](#4-các-chức-năng-đã-hoàn-thành)
5. [Hướng dẫn cài đặt môi trường & Chạy thử](#5-hướng-dẫn-cài-đặt-môi-trường--chạy-thử)
6. [HƯỚNG DẪN VIẾT TIẾP CODE CHO CÁC MODULE CÒN LẠI](#6-hướng-dẫn-viết-tiếp-code-cho-các-module-còn-lại)
   - [Module 1: Đặt hàng & Quản lý đơn hàng (DonHang, ChiTietDonHang) - ĐÃ HOÀN THÀNH](#module-1-đặt-hàng--quản-lý-đơn-hàng-donhang-chitietdonhang)
   - [Module 2: Mã giảm giá (MaGiamGia)](#module-2-mã-giảm-giá-magiamgia)
   - [Module 3: Giao dịch thanh toán (ThanhToan)](#module-3-giao-dịch-thanh-toán-thanhtoan)
   - [Module 4: Vận chuyển & Giao hàng (VanChuyen) - ĐÃ HOÀN THÀNH](#module-4-vận-chuyển--giao-hàng-vanchuyen)
   - [Module 5: Lịch sử tồn kho & Lịch sử trạng thái đơn](#module-5-lịch-sử-tồn-kho--lịch-sử-trạng-thái-đơn)
7. [Quy trình 5 bước chuẩn khi cài đặt một tính năng mới](#7-quy-trình-5-bước-chuẩn-khi-cài-đặt-một-tính-năng-mới)
8. [Các lưu ý kỹ thuật sống còn (Best Practices)](#8-các-lưu-ý-kỹ-thuật-sống-còn-best-practices)

---

## 1. Nguồn tài liệu gốc & Base thiết kế

Toàn bộ mã nguồn được xây dựng bám sát 100% vào các tài liệu đặc tả của nhóm:
* **`Section_2.6_Data_Modeling_Database_Design.docx`**: Tài liệu đặc tả mô hình dữ liệu, từ điển dữ liệu (Data Dictionary), các yêu cầu phi chức năng (NFR01–NFR06) và các Use Case (UC01–UC20).
* **`DataModel_ClassDiagram_ERD.drawio`**: Mô hình lớp khái niệm (Class Diagram gồm 16 classes + 6 enums) và mô hình thực thể liên kết (ERD gồm 14 bảng).
* **`QL_BanLinhKien.sql`**: Script CSDL chuẩn thiết kế cho Microsoft SQL Server 2019+.
* **`QL_BanLinhKien_Data_Sample.sql`**: Dữ liệu mẫu kiểm thử phong phú.

---

## 2. Quy ước lập trình bắt buộc (Coding Conventions)

Theo quy định tại **Section 2.6 Naming convention**, để đảm bảo tính đồng bộ giữa báo cáo phân tích, CSDL SQL và mã nguồn Java:

| Thành phần | Quy tắc đặt tên | Ví dụ trong dự án |
| :--- | :--- | :--- |
| **Bảng & Class Entity** | `PascalCase` tiếng Việt không dấu | `LinhKien`, `GioHang`, `DonHang`, `ChiTietDonHang` |
| **Cột CSDL / Khóa** | `PascalCase`, khóa có tiền tố `Ma` | `MaLinhKien`, `MaDonHang`, `TrangThaiDon` |
| **Thuộc tính & Phương thức** | `camelCase` tiếng Việt không dấu | `maLinhKien`, `themVaoGio()`, `tinhTamTinh()`, `taoDon()` |
| **Enum / Mã trạng thái** | `UPPER_SNAKE_CASE` tiếng Việt không dấu | `DANG_BAN`, `CHO_XAC_NHAN`, `DA_THANH_TOAN`, `COD` |
| **Số tiền / Tiền tệ** | Kiểu `BigDecimal` trong Java (CSDL là `DECIMAL(12,0)`) | Không dùng `double` hay `float` để tránh sai số thập phân |

---

## 3. Kiến trúc phân tầng & Luồng dữ liệu (Architecture & Data Flow)

Dự án áp dụng mô hình phân tầng chuẩn 3 lớp (**3-Tier Architecture / DAO Pattern**):

```
               [ Người dùng / Giao diện Console (UI) ]
                                  │
                                  ▼
                    [ Tầng Dịch vụ (Service Layer) ]
            (Kiểm tra nghiệp vụ, Ràng buộc tồn kho, Validate dữ liệu)
                                  │
                                  ▼
                 [ Tầng Truy xuất CSDL (DAO Layer) ]
            (Thực thi PreparedStatements, Ánh xạ ResultSet)
                                  │
                                  ▼
         [ Cấu hình & Kết nối CSDL (Database Connection Layer) ]
                   │                                  │
                   ▼                                  ▼
      [ Microsoft SQL Server 2019+ ]             [ SQLite Cục bộ ]
       (Môi trường CSDL chính thức)             (Chạy thử nghiệm ngay)
```

### Chi tiết các gói (Packages):
* **`com.linhkien.config`**:
  * `DatabaseConfig`: Đọc cấu hình từ `db.properties`.
  * `DatabaseConnection`: Cung cấp JDBC Connection (hỗ trợ kép cả SQL Server và SQLite).
  * `DatabaseInitializer`: Tự động khởi tạo schema và nạp 10 linh kiện điện tử mẫu ban đầu nếu bảng trống.
* **`com.linhkien.model`**: Chứa các Entity POJO đại diện cho đối tượng nghiệp vụ theo Class Diagram (`LinhKien`, `DanhMucLinhKien`, `GioHang`, `ChiTietGioHang`, `NguoiDung`, `DonHang`, `ChiTietDonHang`, `VanChuyen`, `LichSuTrangThaiDon`). Mỗi lớp chứa cả các phương thức nghiệp vụ hướng đối tượng thuần túy (`kiemTraTon`, `truTon`, `tinhThanhTien`...).
* **`com.linhkien.dao`**: Chịu trách nhiệm duy nhất là truy vấn và thao tác dữ liệu qua SQL (`LinhKienDAO`, `GioHangDAO`, `DanhMucDAO`, `NguoiDungDAO`, `DonHangDAO`).
* **`com.linhkien.service`**: Chứa toàn bộ logic kiểm tra hợp lệ, nghiệp vụ thương mại, cảnh báo tồn kho (`LinhKienService`, `GioHangService`, `DonHangService`).
* **`com.linhkien.ui`**: Giao diện console tương tác (`ConsoleApp`), menu chọn chức năng, định dạng bảng hiển thị.
* **`com.linhkien`**: Điểm khởi chạy chương trình (`Main.java`).
* **`com.linhkien (test)`**: Bộ kiểm thử tự động 54 kiểm tra nghiệp vụ (`AppTest.java`).

---

## 4. Các chức năng đã hoàn thành

### A. Quản lý linh kiện điện tử (UC03–UC05, UC12, UC13)
* [x] **Xem danh sách linh kiện**: Hiển thị bảng chi tiết gồm Mã ID, SKU, Tên linh kiện, Danh mục, Đơn giá, Tồn kho, ĐVT, Trạng thái bán.
* [x] **Tìm kiếm linh kiện**:
  * Tìm theo **Mã ID** (chính xác).
  * Tìm theo **Mã SKU** (chính xác hoặc gần đúng).
  * Tìm kiếm theo **Tên linh kiện** (khớp gần đúng tiếng Việt).
* [x] **Thêm mới linh kiện**: Kiểm tra SKU duy nhất, tên không rỗng, giá $\ge 0$, tồn kho $\ge 0$, danh mục hợp lệ.
* [x] **Cập nhật linh kiện**: Cho phép sửa đổi tên, giá bán, số lượng tồn kho, ngưỡng tồn tối thiểu, trạng thái bán.
* [x] **Xóa / Ngừng bán**: Xóa vật lý nếu chưa phát sinh đơn; tự động chuyển sang `NGUNG_BAN` (soft delete) nếu có ràng buộc khóa ngoại để bảo toàn lịch sử dữ liệu (Section 2.6.4).
* [x] **Cảnh báo tồn kho thấp**: Tự động lọc ra linh kiện có `soLuongTon <= nguongTonToiThieu` (`laTonThap()`).

### B. Chức năng Giỏ hàng (UC06, UC06.1, UC06.2)
* [x] **Xem giỏ hàng & Tính tổng tiền**: Tính thành tiền từng dòng (`soLuong * donGia`) và tổng tạm tính giỏ hàng (`tinhTamTinh()`).
* [x] **Thêm linh kiện vào giỏ**: Kiểm tra sản phẩm đang bán (`DANG_BAN`), **kiểm tra tồn kho** (không cho thêm vượt quá tồn kho thực tế), cộng dồn số lượng nếu đã có trong giỏ.
* [x] **Cập nhật số lượng trong giỏ**: Thay đổi số lượng mua; nếu số lượng $\le 0$ thì tự động xóa dòng; kiểm tra không vượt quá tồn kho.
* [x] **Xóa linh kiện khỏi giỏ** & **Làm trống giỏ hàng**.

### C. Đặt hàng (UC08)
* [x] **Tạo đơn từ giỏ hàng** (`DonHangService.taoDon`): nhập tên, số điện thoại, địa chỉ nhận hàng, phương thức thanh toán (`COD` / `CHUYEN_KHOAN`), ghi chú. Đơn mới có trạng thái `CHO_XAC_NHAN`.
* [x] **Chốt đơn giá** tại thời điểm đặt vào `ChiTietDonHang.DonGia`, nên đổi giá linh kiện sau này không làm thay đổi đơn cũ. `TongThanhToan = TongTienHang + PhiVanChuyen - SoTienGiam` (phí vận chuyển và giảm giá mặc định 0).
* [x] **Trừ tồn kho nguyên tử** (NFR04): `UPDATE ... SET SoLuongTon = SoLuongTon - ? WHERE SoLuongTon >= ? AND TrangThaiBan = 'DANG_BAN'`. Mỗi lần trừ ghi một dòng `LichSuTonKho` (loại `BAN`, tồn trước / tồn sau).
* [x] **Transaction**: tạo đơn, ghi chi tiết, trừ kho, ghi nhật ký và làm trống giỏ chạy trong một giao dịch. Một dòng thiếu tồn thì rollback toàn bộ (không trừ kho các dòng khác, không phát sinh đơn, giữ nguyên giỏ).
* [x] **Chống trùng đơn** (NFR05): mỗi lần xác nhận đặt hàng sinh một `MaYeuCau` (UUID); gửi lại cùng mã sẽ trả về đơn cũ.
* [x] Từ chối khi giỏ trống, số điện thoại sai định dạng, địa chỉ trống, linh kiện đã ngừng bán.

### D. Quản lý đơn hàng & Cập nhật trạng thái vận chuyển (UC10, UC11, UC14, UC19)
* [x] **Khách hàng**: xem lịch sử đơn của mình, xem chi tiết đơn kèm thông tin vận chuyển và lịch sử trạng thái, hủy đơn của mình.
* [x] **Nhân viên bán hàng / quản trị**: xem danh sách mọi đơn (lọc theo trạng thái), xem chi tiết mọi đơn, cập nhật trạng thái.
* [x] **Luồng trạng thái** (mọi chuyển đổi khác đều bị từ chối):

```
CHO_XAC_NHAN --xác nhận--> DANG_DONG_GOI --bàn giao vận chuyển--> DANG_GIAO --+--> DA_GIAO
      |                          |                                            +--> GIAO_THAT_BAI (hoàn kho)
      +----------+---------------+
                 v
              DA_HUY (hoàn kho)
```

* [x] **Bàn giao vận chuyển** (`banGiaoVanChuyen`): nhập đơn vị vận chuyển và mã vận đơn (duy nhất), tạo bản ghi `VanChuyen` trạng thái `DANG_GIAO`, ngày giao dự kiến sau 3 ngày.
* [x] **Giao thành công**: `VanChuyen` chuyển `DA_GIAO` kèm thời điểm giao thực tế; đơn `COD` được ghi nhận đã thu tiền (`DaThuCOD`, `TrangThaiThanhToan = DA_THANH_TOAN`).
* [x] **Hủy đơn / giao thất bại**: cộng lại tồn kho đúng một lần nhờ cờ `DaHoanKho`, ghi `LichSuTonKho` loại `HOAN_HUY`. Khách chỉ hủy được đơn của mình; chỉ hủy được khi đơn đang `CHO_XAC_NHAN` hoặc `DANG_DONG_GOI`.
* [x] **Nhật ký** (NFR03): mọi lần đổi trạng thái ghi `LichSuTrangThaiDon` (trạng thái cũ, mới, người thực hiện, thời điểm, ghi chú).
* [x] **Phân quyền** dựa vào `NguoiDung.kiemTraQuyen("BAN_HANG")`: khách không cập nhật được trạng thái, không xem hay hủy được đơn của người khác.
* Ghi chú thiết kế: bảng `DonHang` còn trạng thái `DA_BAN_GIAO`; luồng hiện tại đi thẳng từ `DANG_DONG_GOI` sang `DANG_GIAO` khi bàn giao, nên không dùng trạng thái này.

### Menu giao diện Console cho phần đặt hàng
| Mục | Chức năng | Người dùng |
| :--- | :--- | :--- |
| 12 | Đặt hàng từ giỏ hàng | Khách hàng (ID 1) |
| 13 | Xem lịch sử đơn hàng của tôi | Khách hàng (ID 1) |
| 14 | Xem chi tiết & theo dõi vận chuyển đơn của tôi | Khách hàng (ID 1) |
| 15 | Hủy đơn hàng của tôi | Khách hàng (ID 1) |
| 16 | Danh sách đơn hàng, lọc theo trạng thái | Nhân viên bán hàng (ID 2) |
| 17 | Xem chi tiết đơn hàng bất kỳ | Nhân viên bán hàng (ID 2) |
| 18 | Cập nhật trạng thái đơn / bàn giao vận chuyển | Nhân viên bán hàng (ID 2) |

Tài khoản mẫu (mật khẩu 123456): ID 1 khách hàng Nguyễn Văn An, ID 2 nhân viên bán hàng Trần Thị Bích, ID 3 nhân viên kho Lê Hoàng Long, ID 4 quản trị viên.

---

## 5. Hướng dẫn cài đặt môi trường & Chạy thử

### Yêu cầu:
* **Java JDK**: Phiên bản 17 hoặc 21 (khuyến nghị Java 21).

### Cấu hình CSDL (`src/main/resources/db.properties`):
* **Chế độ 1 - SQLite (Mặc định - Chạy ngay lập tức)**:
  ```properties
  db.type=sqlite
  sqlite.url=jdbc:sqlite:ql_banlinhkien.db
  db.init.sample_data=true
  ```
  *(Không cần cài thêm bất kỳ phần mềm CSDL nào, hệ thống tự động sinh file `ql_banlinhkien.db` và nạp sẵn 10 linh kiện mẫu).*
* **Chế độ 2 - Microsoft SQL Server 2019+**:
  1. Chạy file `QL_BanLinhKien.sql` rồi chạy file `QL_BanLinhKien_Data_Sample.sql` trong SSMS.
  2. Sửa `db.properties`:
  ```properties
  db.type=sqlserver
  sqlserver.host=localhost
  sqlserver.port=1433
  sqlserver.databaseName=QL_BanLinhKien
  sqlserver.username=sa
  sqlserver.password=your_password
  ```

### Các lệnh thực thi (Windows CMD / PowerShell):
* **Chạy kiểm thử tự động 54 kiểm tra (Automated Test)**:
  ```powershell
  .\test.bat
  # hoặc trong PowerShell:
  powershell -ExecutionPolicy Bypass -File .\test.ps1
  ```
* **Khởi chạy ứng dụng tương tác giao diện Console**:
  ```powershell
  .\run.bat
  # hoặc trong PowerShell:
  powershell -ExecutionPolicy Bypass -File .\run.ps1
  ```
* **Chạy bằng Maven (nếu máy có cài Maven)**:
  ```bash
  mvn compile
  mvn exec:java
  mvn test
  ```

---

## 6. HƯỚNG DẪN VIẾT TIẾP CODE CHO CÁC MODULE CÒN LẠI

Dưới đây là thiết kế chi tiết và mã khung hướng dẫn cho các thành viên trong nhóm khi nhận phân công viết tiếp các chức năng tiếp theo của đề tài.

---

### Module 1: Đặt hàng & Quản lý đơn hàng (DonHang, ChiTietDonHang) - ĐÃ HOÀN THÀNH
> Đã cài đặt trong `DonHang`, `ChiTietDonHang`, `DonHangDAO`, `DonHangService` (xem mục 4.C và 4.D). Phần mô tả dưới đây là thiết kế gốc để đối chiếu.
>
> **Use Case liên quan**: UC08 (Checkout/Tạo đơn), UC10 (Xem lịch sử đơn), UC14 (Xử lý đơn hàng), UC19 (Hủy đơn hàng).  
> **Sequence Diagrams**: CV07.1 (Đặt hàng), CV07.2 (Hủy đơn hàng), CV07.3 (Điều chỉnh kho).

#### 1. Entity `DonHang.java` cần có các thuộc tính:
```java
public class DonHang {
    private int maDonHang;
    private int maNguoiDung;
    private Integer maGiamGia;
    private String maYeuCau;          // Idempotency key tránh trùng đơn (NFR05)
    private LocalDateTime ngayDat;
    private String tenNguoiNhan;
    private String soDienThoaiNhan;
    private String diaChiNhan;
    private BigDecimal tongTienHang;
    private BigDecimal phiVanChuyen;  // Mặc định 0
    private BigDecimal soTienGiam;    // Mặc định 0
    private BigDecimal tongThanhToan; // = tongTienHang + phiVanChuyen - soTienGiam
    private String phuongThucThanhToan; // 'COD', 'CHUYEN_KHOAN'
    private String trangThaiThanhToan;  // 'CHO_THANH_TOAN', 'DA_THANH_TOAN', 'DANG_HOAN_TIEN', 'DA_HOAN_TIEN'
    private String trangThaiDon;        // 'CHO_XAC_NHAN', 'DANG_DONG_GOI', 'DA_BAN_GIAO', 'DANG_GIAO', 'DA_GIAO', 'GIAO_THAT_BAI', 'DA_HUY'
    private boolean daHoanKho;          // Mặc định false (CV07.2)
    private String ghiChu;
    
    // Quan hệ Composition: DonHang ◆ ChiTietDonHang (1 - 1..*)
    private List<ChiTietDonHang> danhSachChiTiet = new ArrayList<>();
}
```

#### 2. Nghiệp vụ cốt lõi trong `DonHangService.java`:
* **Tạo đơn hàng (`taoDon`)**:
  1. Đọc giỏ hàng của khách (`gioHangService.layGioHang(maNguoiDung)`).
  2. Mở Transaction CSDL: `conn.setAutoCommit(false)`.
  3. Kiểm tra và **trừ tồn kho nguyên tử** từng linh kiện (`linhKien.truTon(soLuong)`). Nếu bất kỳ món nào không đủ tồn -> `conn.rollback()` và báo lỗi hết hàng.
  4. Tạo bản ghi `DonHang` và các bản ghi `ChiTietDonHang` (lưu đơn giá cố định tại thời điểm mua).
  5. Ghi lịch sử trạng thái ban đầu vào `LichSuTrangThaiDon` (`trangThaiMoi = 'CHO_XAC_NHAN'`).
  6. Xóa giỏ hàng (`gioHangService.lamTrongGio(maNguoiDung)`).
  7. `conn.commit()`.
* **Hủy đơn hàng (`huyDon`)**:
  1. Kiểm tra trạng thái: Chỉ cho phép hủy khi đơn ở trạng thái `CHO_XAC_NHAN` hoặc `DANG_DONG_GOI`.
  2. Kiểm tra cờ `daHoanKho`: Nếu `daHoanKho == false`, thực hiện cộng hoàn lại tồn kho (`linhKien.hoanTon(soLuong)`), sau đó cập nhật `daHoanKho = true`.
  3. Đổi trạng thái đơn sang `DA_HUY`.
  4. Ghi log vào `LichSuTrangThaiDon`.

---

### Module 2: Mã giảm giá (MaGiamGia)
> **Use Case**: UC09 (Áp dụng mã giảm giá), UC17 (Quản lý mã giảm giá).

#### 1. Entity `MaGiamGia.java`:
```java
public class MaGiamGia {
    private int maGiamGia;
    private String maCode;            // VD: 'GIAM10K', 'SALE10'
    private String loaiGiam;          // 'PHAN_TRAM' hoặc 'SO_TIEN'
    private BigDecimal giaTriGiam;    // % hoặc số tiền cố định
    private BigDecimal donHangToiThieu;
    private Integer soLuotToiDa;
    private int soLuotDaDung;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;

    // Hàm nghiệp vụ
    public boolean kiemTraMa(BigDecimal tongTien) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(ngayBatDau) || now.isAfter(ngayKetThuc)) return false;
        if (soLuotToiDa != null && soLuotDaDung >= soLuotToiDa) return false;
        return tongTien.compareTo(donHangToiThieu) >= 0;
    }

    public BigDecimal tinhSoTienGiam(BigDecimal tongTien) {
        if (!kiemTraMa(tongTien)) return BigDecimal.ZERO;
        if ("PHAN_TRAM".equalsIgnoreCase(loaiGiam)) {
            return tongTien.multiply(giaTriGiam).divide(BigDecimal.valueOf(100));
        }
        return giaTriGiam;
    }
}
```

---

### Module 3: Giao dịch thanh toán (ThanhToan)
> **Use Case**: UC20 (Xác nhận thanh toán thủ công COD / Chuyển khoản).

#### 1. Entity `ThanhToan.java`:
* `maThanhToan`: int
* `maDonHang`: int
* `loaiGiaoDich`: String (`'THU'`, `'HOAN'`)
* `phuongThuc`: String (`'COD'`, `'CHUYEN_KHOAN'`)
* `soTien`: BigDecimal
* `maThamChieu`: String (Mã giao dịch ngân hàng / hóa đơn bưu điện)
* `trangThai`: String (`'CHO_XAC_NHAN'`, `'THANH_CONG'`, `'THAT_BAI'`)
* `maNguoiXacNhan`: Integer (Nhân viên bán hàng / Kế toán xác nhận)
* `thoiDiem`: LocalDateTime

---

### Module 4: Vận chuyển & Giao hàng (VanChuyen) - ĐÃ HOÀN THÀNH
> Đã cài đặt trong `VanChuyen`, `DonHangDAO`, `DonHangService.banGiaoVanChuyen` và `capNhatTrangThai` (xem mục 4.D). Phần mô tả dưới đây là thiết kế gốc để đối chiếu.
>
> **Use Case**: UC11 (Tra cứu vận chuyển), UC14 (Bàn giao cho đơn vị vận chuyển).

#### 1. Entity `VanChuyen.java`:
* `maVanChuyen`: int
* `maDonHang`: int (UNIQUE)
* `donViVanChuyen`: String (GiaoHangNhanh, ViettelPost, J&T...)
* `maVanDon`: String (UNIQUE)
* `trangThaiGiao`: String (`'CHO_LAY_HANG'`, `'DANG_GIAO'`, `'DA_GIAO'`, `'GIAO_THAT_BAI'`, `'HOAN_HANG'`)
* `ngayBanGiao`: LocalDateTime
* `ngayGiaoDuKien`: LocalDate
* `ngayGiaoThucTe`: LocalDateTime
* `daThuCOD`: boolean
* `ghiChu`: String

---

### Module 5: Lịch sử tồn kho & Lịch sử trạng thái đơn
> Phần ghi `LichSuTonKho` (loại `BAN`, `HOAN_HUY`) và `LichSuTrangThaiDon` đã được cài đặt cùng Module 1. Loại `NHAP` và `DIEU_CHINH` khi điều chỉnh kho thủ công vẫn còn để làm tiếp.
>
> **Yêu cầu phi chức năng NFR03 (Audit Trail)**: Mọi biến động số lượng tồn và trạng thái đơn đều phải lưu log dấu vết.

* **`LichSuTonKho`**: Lưu `maLinhKien`, `loaiBienDong` (`'NHAP'`, `'BAN'`, `'HOAN_HUY'`, `'DIEU_CHINH'`), `soLuongThayDoi`, `tonTruoc`, `tonSau`, `maDonHang`, `maNguoiThucHien`, `lyDo`, `thoiDiem`.
* **`LichSuTrangThaiDon`**: Lưu `maDonHang`, `trangThaiCu`, `trangThaiMoi`, `maNguoiThucHien`, `thoiDiem`, `ghiChu`.

---

## 7. Quy trình 5 bước chuẩn khi cài đặt một tính năng mới

Khi nhận phân công viết một tính năng mới (ví dụ: Tạo đơn hàng `DonHang`), bạn hãy thực hiện theo đúng thứ tự 5 bước sau:

### Bước 1: Tạo Lớp Model (Entity) trong `com.linhkien.model`
- Khai báo các thuộc tính đúng theo Class Diagram trong `DataModel_ClassDiagram_ERD.drawio`.
- Viết Constructor, Getter, Setter và các hàm nghiệp vụ của riêng đối tượng đó.

### Bước 2: Tạo Lớp DAO trong `com.linhkien.dao`
- Mẫu cơ bản lấy Connection:
  ```java
  Connection conn = DatabaseConnection.getConnection();
  ```
- Luôn dùng `PreparedStatement` với tham số `?` để chống SQL Injection.
- Nếu chèn bản ghi tự sinh khóa chính: dùng `Statement.RETURN_GENERATED_KEYS`.

### Bước 3: Tạo Lớp Service trong `com.linhkien.service`
- Đây là nơi kiểm tra hợp lệ dữ liệu (Validate) và các quy tắc nghiệp vụ:
  - Giá trị không âm, chuỗi không rỗng.
  - Kiểm tra tồn kho, kiểm tra trạng thái trước khi cho phép chuyển bước.
- Ném ra `IllegalArgumentException` hoặc `IllegalStateException` khi vi phạm điều kiện.

### Bước 4: Viết kịch bản kiểm thử tự động trong `src/test/java/com/linhkien/AppTest.java`
- Thêm một phương thức `testModuleMoi()` vào `AppTest.java`.
- Kiểm tra các trường hợp: Thành công, thất bại, dữ liệu biên.
- Chạy `.\test.bat` để đảm bảo không làm gãy code cũ.

### Bước 5: Tích hợp vào Menu giao diện Console trong `com.linhkien.ui.ConsoleApp`
- Thêm mục menu mới vào hàm `printMainMenu()`.
- Thêm `case` xử lý trong vòng lặp `run()` và viết hàm nhập liệu tương ứng.

---

## 8. Các lưu ý kỹ thuật sống còn (Best Practices)

1. **Quản lý Transaction (Giao dịch đa bảng)**:
   Khi một thao tác ảnh hưởng đến nhiều bảng (ví dụ: Tạo đơn hàng = Ghi `DonHang` + Ghi `ChiTietDonHang` + Trừ `LinhKien` + Xóa `ChiTietGioHang`):
   ```java
   Connection conn = DatabaseConnection.getConnection();
   try {
       conn.setAutoCommit(false); // Bắt đầu transaction
       
       // Các thao tác ghi CSDL...
       
       conn.commit(); // Thành công thì lưu
   } catch (Exception e) {
       conn.rollback(); // Có lỗi thì hoàn tác toàn bộ
       throw e;
   } finally {
       conn.setAutoCommit(true);
   }
   ```
2. **Không đóng nhầm Connection dùng chung**:
   Lớp `DatabaseConnection` sử dụng kết nối dùng chung trong suốt phiên làm việc. Trong các phương thức DAO, chỉ cần đóng `PreparedStatement` và `ResultSet` (dùng cú pháp `try-with-resources`). Không gọi `conn.close()` trong các hàm DAO con, chỉ đóng kết nối khi thoát toàn bộ chương trình (tại `Main` hoặc `ConsoleApp`).
3. **Quy tắc xóa linh kiện**:
   Tuyệt đối **không** dùng `DELETE FROM LinhKien` với linh kiện đã phát sinh đơn hàng, mà phải đổi trạng thái sang `'NGUNG_BAN'` (Soft Delete) theo đúng mục **2.6.4** trong tài liệu báo cáo.
4. **Hỗ trợ CSDL kép**:
   Khi viết câu lệnh SQL mới, hãy viết cú pháp chuẩn ANSI SQL để chạy tương thích tốt trên cả SQLite và Microsoft SQL Server 2019.

---

> 🎯 *Chúc nhóm hoàn thành xuất sắc đồ án môn học PTTKYC! Nếu có bất kỳ câu hỏi nào về kiến trúc hoặc luồng code, hãy tham khảo các file mẫu có sẵn: `LinhKienDAO.java`, `GioHangService.java` và `AppTest.java`.*
