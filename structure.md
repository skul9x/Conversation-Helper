# Project Structure

Dưới đây là cấu trúc thư mục và các thành phần chính của dự án **Conversation Helper**.

```text
Conversation/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/skul9x/conversation/
│   │   │   │   ├── AppDatabase.kt           # Cấu hình Room Database
│   │   │   │   ├── ConversationApplication.kt # Lớp Application để khởi tạo Repository
│   │   │   │   ├── MainActivity.kt          # Màn hình chính, xử lý UI và TTS
│   │   │   │   ├── MainViewModel.kt         # Logic xử lý dữ liệu và tương tác với Repository
│   │   │   │   ├── Sentence.kt              # Data class đại diện cho một câu (Entity)
│   │   │   │   ├── SentenceAdapter.kt       # Adapter cho RecyclerView hiển thị danh sách câu
│   │   │   │   ├── SentenceDao.kt           # Data Access Object cho Room
│   │   │   │   └── SentenceRepository.kt    # Lớp trung gian quản lý dữ liệu
│   │   │   └── res/
│   │   │       ├── layout/                  # Các file giao diện XML
│   │   │       │   ├── activity_main.xml
│   │   │       │   ├── dialog_add_edit_sentence.xml
│   │   │       │   └── item_sentence.xml
│   │   │       ├── values/                  # Chuỗi văn bản, màu sắc, style
│   │   │       └── menu/                    # Menu cho Toolbar (Backup/Restore)
│   ├── build.gradle.kts                     # Cấu hình build cho module app
├── build.gradle.kts                         # Cấu hình build cho toàn bộ project
├── settings.gradle.kts                      # Cấu hình các module trong project
└── README.md                                # Tài liệu hướng dẫn sử dụng
```

## 🧩 Các thành phần chính

### 1. Data Layer
- **Sentence.kt**: Định nghĩa cấu trúc dữ liệu của một câu, bao gồm ID, nội dung tiếng Trung, chú thích tiếng Việt và chỉ số thứ tự.
- **SentenceDao.kt**: Cung cấp các phương thức để truy vấn, thêm, sửa, xóa dữ liệu trong SQLite.
- **AppDatabase.kt**: Khởi tạo cơ sở dữ liệu Room.

### 2. Domain Layer
- **SentenceRepository.kt**: Quản lý việc truy xuất dữ liệu từ DAO, giúp tách biệt logic dữ liệu khỏi UI.

### 3. UI Layer (MVVM)
- **MainViewModel.kt**: Nhận dữ liệu từ Repository và cung cấp dưới dạng `Flow` cho Activity. Xử lý các logic như thêm, sửa, xóa và sắp xếp lại thứ tự câu.
- **MainActivity.kt**: Hiển thị danh sách câu, xử lý các sự kiện người dùng (click để đọc, nhấn giữ để sửa/xóa) và tích hợp Android TTS.
- **SentenceAdapter.kt**: Quản lý việc hiển thị từng mục câu trong `RecyclerView`.

### 4. Backup & Restore
- Logic sao lưu và phục hồi được tích hợp trực tiếp trong `MainActivity.kt`, sử dụng `ActivityResultContracts` để chọn file và `JSONObject/JSONArray` để xử lý dữ liệu JSON.
