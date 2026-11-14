package com.linhkien.api;

import com.linhkien.config.DatabaseInitializer;
import com.linhkien.dao.DanhMucDAO;
import com.linhkien.service.DiaChiService;
import com.linhkien.service.DonHangService;
import com.linhkien.service.GioHangService;
import com.linhkien.service.LinhKienService;
import com.linhkien.service.MaGiamGiaService;
import com.linhkien.service.ThanhToanService;
import com.linhkien.util.HttpRequestParams;
import com.linhkien.util.JsonSerializer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * HTTP Router cục bộ cho Web UI & API smoke tests.
 * Tài khoản demo: khách hàng 1, nhân viên bán hàng 2, nhân viên kho 3, quản trị viên 4.
 */
public class ApiServer {
    static final LinhKienService products = new LinhKienService();
    static final GioHangService cart = new GioHangService();
    static final DonHangService orders = new DonHangService();
    static final MaGiamGiaService coupons = new MaGiamGiaService();
    static final ThanhToanService payments = new ThanhToanService();
    static final DiaChiService accounts = new DiaChiService();
    static final DanhMucDAO danhMucDAO = new DanhMucDAO();

    private static final List<ApiController> CONTROLLERS = List.of(
        new ProductApiController(products, danhMucDAO),
        new CartApiController(cart),
        new OrderApiController(orders, payments),
        new CouponApiController(coupons, cart),
        new PaymentApiController(payments),
        new AccountApiController(accounts)
    );

    public static void main(String[] args) throws Exception {
        DatabaseInitializer.initializeDatabase();
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        // Single-threaded executor preserves transaction boundaries on the shared JDBC connection.
        server.createContext("/api", ApiServer::handle);
        server.start();
        System.out.println("Prototype API: http://127.0.0.1:" + port + "/api/products");
    }

    static void handle(HttpExchange exchange) throws IOException {
        int status = 200;
        Object result;
        try {
            HttpRequestParams params = HttpRequestParams.fromExchange(exchange);
            String path = exchange.getRequestURI().getPath();
            String route = path.length() >= 4 ? path.substring(4) : "/";
            String method = exchange.getRequestMethod();

            Object handled = ApiController.UNHANDLED;
            for (ApiController controller : CONTROLLERS) {
                handled = controller.handle(route, method, params);
                if (handled != ApiController.UNHANDLED) {
                    break;
                }
            }
            if (handled == ApiController.UNHANDLED) {
                status = 404;
                result = Map.of("message", "API không tồn tại");
            } else {
                result = handled;
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            status = 400;
            result = Map.of("message", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            status = 500;
            result = Map.of("message", "Không thể xử lý dữ liệu. Kiểm tra backend.");
        }

        byte[] data = JsonSerializer.toJson(result).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, data.length);
        exchange.getResponseBody().write(data);
        exchange.close();
    }
}
