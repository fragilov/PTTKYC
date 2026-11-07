package com.linhkien;

import com.linhkien.config.DatabaseInitializer;
import com.linhkien.ui.ConsoleApp;

/**
 * Điểm khởi chạy chính của chương trình (Entry Point).
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("  KHỞI ĐỘNG HỆ THỐNG QUẢN LÝ LINH KIỆN ĐIỆN TỬ VÀ GIỎ HÀNG (PTTKYC)");
        System.out.println("=========================================================================");

        // 1. Khởi tạo và kiểm tra kết nối CSDL (Tự động nạp bảng và dữ liệu mẫu nếu cần)
        DatabaseInitializer.initializeDatabase();

        // 2. Chạy giao diện điều khiển Console tương tác
        ConsoleApp app = new ConsoleApp();
        app.run();
    }
}
