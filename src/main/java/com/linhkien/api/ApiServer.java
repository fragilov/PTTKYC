package com.linhkien.api;

import com.sun.net.httpserver.*;
import com.linhkien.config.DatabaseInitializer;
import com.linhkien.service.*;
import com.linhkien.model.*;
import com.linhkien.dao.DanhMucDAO;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.*;
import java.lang.reflect.*;

/** Local prototype adapter: customer 1 and administrator 4 are seeded demo identities. */
public class ApiServer {
    static final LinhKienService products = new LinhKienService();
    static final GioHangService cart = new GioHangService();
    static final DonHangService orders = new DonHangService();
    public static void main(String[] args) throws Exception {
        DatabaseInitializer.initializeDatabase();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", args.length > 0 ? Integer.parseInt(args[0]) : 8080), 0);
        // One executor preserves the existing shared JDBC connection's transaction boundaries.
        server.createContext("/api", ApiServer::handle);
        server.start();
        System.out.println("Prototype API: http://127.0.0.1:8080/api/products");
    }
    static void handle(HttpExchange x) throws java.io.IOException {
        int status = 200; Object result;
        try {
            Map<String,String> p = new HashMap<>();
            String query = x.getRequestURI().getRawQuery();
            if (query != null) parse(query,p);
            parse(new String(x.getRequestBody().readAllBytes(), StandardCharsets.UTF_8),p);
            String route = x.getRequestURI().getPath().substring(4);
            String method = x.getRequestMethod();
            int id = Integer.parseInt(p.getOrDefault("id","0"));
            if (route.equals("/products") && method.equals("GET")) result = products.layTatCaLinhKien();
            else if (route.equals("/categories") && method.equals("GET")) result = new DanhMucDAO().findAll();
            else if (route.equals("/products") && method.equals("POST")) {
                LinhKien lk = id == 0 ? new LinhKien() : products.timTheoMa(id);
                if (lk == null) throw new IllegalArgumentException("Không tìm thấy linh kiện");
                lk.setMaSKU(p.get("sku")); lk.setTenLinhKien(p.get("name"));
                lk.setMaDanhMuc(Integer.parseInt(p.get("category")));
                lk.setGiaBan(new BigDecimal(p.get("price"))); lk.setSoLuongTon(Integer.parseInt(p.get("stock")));
                lk.setNhaSanXuat(p.getOrDefault("maker","")); lk.setKieuDongGoi("MODULE");
                if(id == 0) products.themLinhKien(lk); else products.capNhatLinhKien(lk);
                result = lk;
            } else if(route.equals("/products") && method.equals("DELETE")) result = Map.of("deleted", products.xoaLinhKien(id));
            else if(route.equals("/cart") && method.equals("GET")) result = cart.layGioHang(1);
            else if(route.equals("/cart") && method.equals("POST")) {
                int qty = Integer.parseInt(p.get("quantity"));
                if ("add".equals(p.get("action"))) cart.themVaoGio(1,id,qty);
                else if(qty == 0) cart.xoaKhoiGio(1,id); else cart.capNhatSoLuong(1,id,qty);
                result = cart.layGioHang(1);
            } else if(route.equals("/orders") && method.equals("GET")) result = "true".equals(p.get("admin")) ? orders.layTatCaDon(4,null) : orders.layDonCuaKhach(1);
            else if(route.equals("/orders") && method.equals("POST")) result = orders.taoDon(1,p.get("name"),p.get("phone"),p.get("address"),p.get("payment"),p.get("requestId"),p.getOrDefault("note",""),p.getOrDefault("shipping","standard"));
            else if(route.equals("/tracking") && method.equals("GET")) {
                DonHang order = orders.layChiTietDon(id,1);
                Map<String,Object> data = new HashMap<>(); data.put("order",order); data.put("history",orders.layLichSuTrangThai(id)); data.put("shipping",orders.layVanChuyen(id)); result=data;
            } else if(route.equals("/status") && method.equals("POST")) {
                orders.capNhatTrangThai(id,p.get("status"),4,"Cập nhật từ prototype UI"); result=Map.of("ok",true);
            } else if(route.equals("/shipping") && method.equals("POST")) result = orders.banGiaoVanChuyen(id,p.get("carrier"),p.get("tracking"),4);
            else { status=404; result=Map.of("message","API không tồn tại"); }
        } catch (IllegalArgumentException | IllegalStateException e) {status=400; result=Map.of("message",e.getMessage());}
        catch(Exception e) {e.printStackTrace(); status=500; result=Map.of("message","Không thể xử lý dữ liệu. Kiểm tra backend.");}
        byte[] data=json(result).getBytes(StandardCharsets.UTF_8);
        x.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");
        x.sendResponseHeaders(status,data.length); x.getResponseBody().write(data); x.close();
    }
    static void parse(String value,Map<String,String> p) {
        for(String pair:value.split("&")) {String[] parts=pair.split("=",2); if(parts.length==2) p.put(URLDecoder.decode(parts[0],StandardCharsets.UTF_8),URLDecoder.decode(parts[1],StandardCharsets.UTF_8));}
    }
    static String json(Object v) {
        if(v==null) return "null";
        if(v instanceof Number || v instanceof Boolean) return v.toString();
        if(v instanceof Map<?,?> m) return "{"+String.join(",",m.entrySet().stream().map(e->json(e.getKey().toString())+":"+json(e.getValue())).toList())+"}";
        if(v instanceof Iterable<?> it) {List<String> values=new ArrayList<>(); for(Object item:it) values.add(json(item)); return "["+String.join(",",values)+"]";}
        if(v.getClass().getPackageName().startsWith("com.linhkien.model")) {
            Map<String,Object> fields=new TreeMap<>();
            for(Method m:v.getClass().getMethods()) if(m.getParameterCount()==0 && m.getName().startsWith("get") && !m.getName().equals("getClass")) {
                try {String n=m.getName().substring(3); fields.put(Character.toLowerCase(n.charAt(0))+n.substring(1),m.invoke(v));} catch(Exception e) {throw new IllegalStateException(e);}
            }
            return json(fields);
        }
        String s=v.toString(); StringBuilder b=new StringBuilder("\"");
        for(char c:s.toCharArray()) {if(c=='"'||c=='\\') b.append('\\').append(c); else if(c<32) b.append(String.format("\\u%04x",(int)c)); else b.append(c);}
        return b.append('"').toString();
    }
}
