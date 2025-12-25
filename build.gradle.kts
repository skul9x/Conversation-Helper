// build.gradle.kts (Project: Conversation)
// TỆP NÀY NẰM Ở THƯ MỤC GỐC, BÊN NGOÀI THƯ MỤC 'app'

plugins {
    // Phiên bản Android Gradle Plugin, bạn có thể giữ phiên bản cũ của mình
    id("com.android.application") version "8.2.2" apply false

    // Phiên bản Kotlin, bạn có thể giữ phiên bản cũ của mình
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false

    // DÒNG QUAN TRỌNG CẦN THÊM VÀO:
    // Khai báo phiên bản KSP để Room có thể hoạt động
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}