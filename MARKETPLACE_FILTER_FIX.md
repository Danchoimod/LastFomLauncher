# Fix Marketplace Spinner Filter

## Vấn đề ban đầu

Spinner lọc sản phẩm trong Marketplace không hoạt động vì:

1. **Giá trị trong strings.xml không khớp với dữ liệu Firestore**
   - Trước: `["All", "Addon", "Texture", "Skins", "map"]`
   - Firestore data: `["maps", "skin", "texture pack", "addon"]`
   - Không khớp → lọc không ra kết quả

2. **Hàm mapSpinnerToType() map sai**
   - Map "Skins" → "skins" (sai, phải là "skin")
   - Map "map" → "maps" (đúng nhưng không consistent)
   - Map "Texture" → "texture pack" (đúng)

## Giải pháp đã áp dụng

### 1. Cập nhật strings.xml

**File**: `app/src/main/res/values/strings.xml`

```xml
<array name="pack_type_spinenr">
    <item>All</item>
    <item>Maps</item>
    <item>Skin</item>
    <item>Texture Pack</item>
    <item>Addon</item>
</array>
```

Thay đổi:
- `"map"` → `"Maps"` (capitalize và số nhiều)
- `"Skins"` → `"Skin"` (số ít)
- `"Texture"` → `"Texture Pack"` (đầy đủ)
- Sắp xếp lại thứ tự cho dễ nhìn

### 2. Fix hàm mapSpinnerToType()

**File**: `app/src/main/java/org/levimc/launcher/ui/fragment/Marketplace.java`

```java
private String mapSpinnerToType(String value) {
    if (value == null) return "all";
    String v = value.trim().toLowerCase();
    // Map display text to exact Firestore field values
    if (v.contains("all") || v.contains("tất cả")) return "all";
    if (v.contains("map")) return "maps";                    // "Maps" -> "maps"
    if (v.contains("skin")) return "skin";                   // "Skin" -> "skin"
    if (v.contains("texture") || v.contains("resource")) return "texture pack"; // "Texture Pack" -> "texture pack"
    if (v.contains("addon") || v.contains("add-on")) return "addon"; // "Addon" -> "addon"
    return v; // fallback: use as-is
}
```

**Logic hoạt động:**
1. Chuyển input về lowercase để so sánh không phân biệt hoa thường
2. Dùng `contains()` thay vì `equals()` để linh hoạt hơn
3. Map chính xác sang giá trị Firestore:
   - "Maps" (UI) → "maps" (Firestore)
   - "Skin" (UI) → "skin" (Firestore)
   - "Texture Pack" (UI) → "texture pack" (Firestore)
   - "Addon" (UI) → "addon" (Firestore)

### 3. Thêm logging để debug

Đã thêm Log statements để kiểm tra:
- Giá trị spinner được chọn
- Giá trị sau khi map
- Query type đang được sử dụng
- Số lượng items được tải
- Type của mỗi item

## Debug với Logcat

Sau khi chạy app, mở **Logcat** và filter theo tag `Marketplace`:

```
adb logcat -s Marketplace
```

Khi bạn chọn spinner, bạn sẽ thấy:
```
D/Marketplace: Spinner selected: 'Maps' -> mapped to: 'maps'
D/Marketplace: Filtering by type: maps
D/Marketplace: Item: Example Map | Type: maps
D/Marketplace: Loaded 5 items
```

### Các lỗi thường gặp:

#### 1. "Không tìm thấy items cho type: xxx"
**Nguyên nhân**: Không có dữ liệu với type đó trong Firestore
**Giải pháp**: 
- Kiểm tra Firestore Console
- Xem field "type" của documents có đúng giá trị không
- Đảm bảo viết đúng: `"maps"`, `"skin"`, `"texture pack"`, `"addon"` (lowercase, có dấu cách)

#### 2. Query fails với lỗi index
**Lỗi**: `FAILED_PRECONDITION: The query requires an index`

**Nguyên nhân**: Firestore cần tạo composite index khi query kết hợp:
- `whereEqualTo("type", ...)` + `orderBy("createdAt")`

**Giải pháp có 2 cách:**

**Cách 1: Tạo index (Khuyến nghị cho production)**
1. Khi lỗi xuất hiện, copy link tạo index từ error message
2. Paste vào browser và đăng nhập Firebase Console
3. Click "Create Index"
4. Đợi 2-5 phút để index được tạo
5. Test lại app

**Cách 2: Sửa code không cần index (Đã áp dụng)**
- Code đã được fix: khi filter theo type, không sort theo `createdAt`
- Items sẽ hiện theo thứ tự mặc định của Firestore
- Ưu điểm: không cần tạo index
- Nhược điểm: items không được sort theo thời gian tạo khi filter

**Code đã fix:**
```java
// Chỉ sort theo createdAt khi KHÔNG filter theo type
if (!TextUtils.isEmpty(searchQuery)) {
    q = q.orderBy("name").startAt(searchQuery).endAt(searchQuery + "\uf8ff");
} else if (!hasTypeFilter) {
    q = q.orderBy("createdAt", Query.Direction.DESCENDING);
}
```

**Nếu muốn sort cả khi filter**, bạn PHẢI tạo index theo Cách 1

#### 3. Spinner map sai type
**Nguyên nhân**: mapSpinnerToType() return sai giá trị
**Giải pháp**:
- Xem Logcat xem value được map thành gì
- Kiểm tra lại hàm mapSpinnerToType()

## Dữ liệu Firestore phải có format

```json
{
  "marketplace": [
    {
      "id": "doc_id",
      "name": "Pack name",
      "type": "maps",        // ⚠️ PHẢI LÀ lowercase: "maps", "skin", "texture pack", "addon"
      "price": 100,
      "description": "...",
      "image": "url",
      "url": "download_url",
      "owner": "owner_name",
      "ownerUrl": "owner_url",
      "packId": 1,
      "createdAt": "2025-01-01T00:00:00Z"
    }
  ]
}
```

### Kiểm tra dữ liệu Firestore:

1. Mở Firebase Console
2. Vào Firestore Database
3. Mở collection `marketplace`
4. Kiểm tra từng document:
   - Field `type` có tồn tại không?
   - Giá trị có đúng là một trong: `"maps"`, `"skin"`, `"texture pack"`, `"addon"` không?
   - Không có chữ in hoa, không có dấu cách thừa

### Fix dữ liệu Firestore nếu sai:

Nếu type sai (ví dụ: "Maps", "skins", "Texture", etc.), cần update:

```javascript
// Trong Firebase Console hoặc dùng script
// Ví dụ fix bằng console:
// 1. Click vào document
// 2. Click edit field "type"
// 3. Đổi thành giá trị đúng: "maps", "skin", "texture pack", hoặc "addon"
// 4. Lưu lại
```

Hoặc dùng script để batch update:

```javascript
// Run trong Firebase Console > Firestore > Rules & Indexes > Query
const batch = db.batch();

// Fix "Skins" -> "skin"
db.collection('marketplace').where('type', '==', 'Skins').get()
  .then(snapshot => {
    snapshot.forEach(doc => {
      batch.update(doc.ref, { type: 'skin' });
    });
    return batch.commit();
  });

// Fix "Map" -> "maps"
db.collection('marketplace').where('type', '==', 'Map').get()
  .then(snapshot => {
    snapshot.forEach(doc => {
      batch.update(doc.ref, { type: 'maps' });
    });
    return batch.commit();
  });
```

## Cách test

1. **Build và chạy app**
   ```cmd
   gradlew assembleDebug
   ```

2. **Vào màn hình Marketplace**

3. **Mở Logcat để xem logs**
   ```cmd
   adb logcat -s Marketplace
   ```

4. **Chọn spinner filter** ở góc trên

5. **Thử lọc từng loại và xem log:**
   - All → hiện tất cả
   - Maps → chỉ hiện items có type = "maps"
   - Skin → chỉ hiện items có type = "skin"
   - Texture Pack → chỉ hiện items có type = "texture pack"
   - Addon → chỉ hiện items có type = "addon"

6. **Kiểm tra kết quả:**
   - Nếu không có items: xem log để biết query có chạy không
   - Nếu có lỗi: xem error message trong Toast
   - Nếu query fails: kiểm tra Firestore index

## Tham khảo React code

Đoạn code React mẫu dùng logic tương tự:

```typescript
const PACK_TYPES = ['maps', 'skin', 'texture pack', 'addon'];

const filterItems = (type: string) => {
  setSelectedType(type);
  if (type === 'all') {
    setFilteredItems(items);
  } else {
    setFilteredItems(items.filter(item => item.type === type));
  }
};
```

Logic giống nhau:
- Có danh sách types cố định
- Filter dựa trên exact match với field "type"
- "all" → hiện tất cả
- Các type khác → filter theo điều kiện

## Crash Handler

App đã có crash handler để bắt lỗi:
- Khi crash, app sẽ hiện màn hình lỗi thay vì thoát
- Có nút sao chép lỗi để debug
- Có nút khởi động lại app
- Xem chi tiết trong `CRASH_HANDLER_README.md`

## Notes

- Nếu thêm type mới, cần update cả strings.xml và mapSpinnerToType()
- Type trong Firestore phải khớp chính xác (case-sensitive)
- Dùng lowercase khi query để tránh lỗi case mismatch
- Luôn kiểm tra Logcat khi debug filter
- Đảm bảo dữ liệu Firestore có field "type" với giá trị đúng

