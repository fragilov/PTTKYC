(async()=>{
const fs=require('fs'),os=require('os'),path=require('path'),{spawn}=require('child_process'),assert=require('assert/strict');
const root=process.cwd(), dir=fs.mkdtempSync(path.join(os.tmpdir(),'volthub-api-'));
const proc=spawn(path.join(require('child_process').execFileSync('where.exe',['javac'],{encoding:'utf8'}).trim().split(/\r?\n/)[0], '..', 'java.exe'),['-Dfile.encoding=UTF-8','-cp',path.join(root,'bin')+';'+path.join(root,'lib/*'),'com.linhkien.api.ApiServer','18080'],{cwd:dir});
let log='';proc.stdout.on('data',x=>log+=x);proc.stderr.on('data',x=>log+=x);
async function api(p,data,method='POST'){const r=await fetch('http://127.0.0.1:18080/api'+p,data?{method,body:new URLSearchParams(data)}:{});const b=await r.json();if(!r.ok)throw Error(JSON.stringify(b));return b}
try{
for(let i=0;i<50;i++){try{await api('/products');break}catch{await new Promise(r=>setTimeout(r,200))}}
const products=await api('/products'),p=products[0];assert(products.length>0);
const categories=await api('/categories');
await api('/products',{sku:'TEST-API',name:'Linh kiện API',category:categories[0].maDanhMuc,price:10000,stock:5});
let created=(await api('/products')).find(x=>x.maSKU==='TEST-API');assert(created);
await api('/products',{id:created.maLinhKien,sku:'TEST-API',name:'Đã sửa',category:categories[0].maDanhMuc,price:12000,stock:6});
assert.equal((await api('/products')).find(x=>x.maSKU==='TEST-API').tenLinhKien,'Đã sửa');
await api('/products',{id:created.maLinhKien},'DELETE');
const basket=await api('/cart');for(const l of basket.danhSachChiTiet) await api('/cart',{id:l.maLinhKien,quantity:0});
await api('/cart',{id:p.maLinhKien,quantity:1,action:'add'});await api('/cart',{id:p.maLinhKien,quantity:2});
const request={name:'Khách API',phone:'0901234567',address:'12 Nguyễn Huệ, TP.HCM',payment:'COD',requestId:'api-smoke-1',shipping:'express'};
const order=await api('/orders',request);assert.equal(order.tongThanhToan,p.giaBan*2+49000);assert.equal((await api('/cart')).danhSachChiTiet.length,0);
assert.equal((await api('/products')).find(x=>x.maLinhKien===p.maLinhKien).soLuongTon,p.soLuongTon-2);
assert.equal((await api('/orders',request)).maDonHang,order.maDonHang);
await api('/status',{id:order.maDonHang,status:'DANG_DONG_GOI'});await api('/shipping',{id:order.maDonHang,carrier:'GHN',tracking:'API-001'});await api('/status',{id:order.maDonHang,status:'DA_GIAO'});
const tracking=await api('/tracking?id='+order.maDonHang);assert.equal(tracking.order.trangThaiDon,'DA_GIAO');assert.equal(tracking.shipping.maVanDon,'API-001');assert(tracking.history.length>=4);
await api('/cart',{id:p.maLinhKien,quantity:1,action:'add'});const cancelled=await api('/orders',{...request,requestId:'api-smoke-2',shipping:'standard'});await api('/status',{id:cancelled.maDonHang,status:'DA_HUY'});assert.equal((await api('/products')).find(x=>x.maLinhKien===p.maLinhKien).soLuongTon,p.soLuongTon-2);
console.log('PASS: product CRUD, cart update, checkout + shipping fee, stock deduction, idempotency, shipment tracking, delivery, cancellation + restock. Isolated database: '+dir);
}catch(e){console.error(log);throw e}finally{proc.kill()}

})().catch(e=>{console.error(e);process.exitCode=1})
