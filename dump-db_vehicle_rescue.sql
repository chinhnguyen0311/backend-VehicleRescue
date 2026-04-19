-- 1. Kích hoạt extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;

-- 2. Tạo bảng accounts (Không có khóa ngoại)
CREATE TABLE accounts (
    account_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    refresh_token TEXT,
    role VARCHAR(50) NOT NULL,
    google_id VARCHAR(255),
    avatar_url VARCHAR(500),
    last_active TIMESTAMP WITH TIME ZONE,
    banned_at TIMESTAMP WITH TIME ZONE,
    suspended_until TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tạo bảng services (Không có khóa ngoại)
CREATE TABLE services (
    service_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    icon_url VARCHAR(500),
    base_price NUMERIC(15, 2) NOT NULL
);

-- 4. Tạo bảng notifications (Phụ thuộc accounts)
CREATE TABLE notifications (
    notification_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_id UUID,
    related_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    is_sent BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_noti_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

-- 5. Tạo bảng mechanics (Phụ thuộc accounts)
CREATE TABLE mechanics (
    mechanic_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id UUID UNIQUE NOT NULL, -- UNIQUE để đảm bảo quan hệ 1-1
    type VARCHAR(50) NOT NULL,
    work_type VARCHAR(50) NOT NULL DEFAULT 'MOBILE',
    display_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    description TEXT,
    current_location GEOMETRY(Point, 4326), -- Tọa độ PostGIS (Kinh độ, Vĩ độ)
    garage_name VARCHAR(255),                          -- Chỉ có khi work_type = GARAGE
    garage_address VARCHAR(500),                       -- Chỉ có khi work_type = GARAGE
    garage_location GEOMETRY(Point, 4326),
    status VARCHAR(50) DEFAULT 'OFFLINE',
    rating_score NUMERIC(3, 2) DEFAULT 0.00,
    total_reviews INTEGER DEFAULT 0,
    subs_end_date TIMESTAMP,
    is_active_subs BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_mechanic_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

-- 6. Tạo bảng trung gian mechanic_services (Phụ thuộc mechanics và services)
CREATE TABLE mechanic_services (
    mechanic_id UUID NOT NULL,
    service_id UUID NOT NULL,
    custom_price NUMERIC(15, 2) NOT NULL,
    PRIMARY KEY (mechanic_id, service_id),
    CONSTRAINT fk_ms_mechanic FOREIGN KEY (mechanic_id) REFERENCES mechanics(mechanic_id) ON DELETE CASCADE,
    CONSTRAINT fk_ms_service FOREIGN KEY (service_id) REFERENCES services(service_id) ON DELETE CASCADE
);

-- 7. Tạo bảng rescue_orders (Phụ thuộc mechanics và services)
CREATE TABLE rescue_orders (
    order_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    customer_location GEOMETRY(Point, 4326) NOT NULL, -- Tọa độ khách hàng
    customer_address TEXT,
    mechanic_id UUID,
    service_id UUID,
    status VARCHAR(50) DEFAULT 'REQUESTED',
    mechanic_name VARCHAR(255), -- Snapshot tên thợ
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_mechanic FOREIGN KEY (mechanic_id) REFERENCES mechanics(mechanic_id) ON DELETE SET NULL,
    CONSTRAINT fk_order_service FOREIGN KEY (service_id) REFERENCES services(service_id) ON DELETE SET NULL
);

-- 8. Tạo bảng reviews (Phụ thuộc rescue_orders và mechanics)
CREATE TABLE reviews (
    review_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID UNIQUE NOT NULL, -- UNIQUE để đảm bảo mỗi đơn chỉ được đánh giá 1 lần (Quan hệ 1-1)
    mechanic_id UUID NOT NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    review TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES rescue_orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_review_mechanic FOREIGN KEY (mechanic_id) REFERENCES mechanics(mechanic_id) ON DELETE CASCADE
);

-- 9. Tạo bảng reports (Phụ thuộc rescue_orders)
CREATE TABLE reports (
    report_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL,
    reported_by_type VARCHAR(50) NOT NULL,
    reporter_phone VARCHAR(20) NOT NULL,
    reason_category VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    admin_note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_order FOREIGN KEY (order_id) REFERENCES rescue_orders(order_id) ON DELETE CASCADE
);
CREATE TABLE mechanic_subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mechanic_id UUID NOT NULL,
    current_end_date TIMESTAMP,   -- ngày hết hạn hiện tại
    new_end_date TIMESTAMP,       -- ngày hết hạn sau khi gia hạn
    renewal_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    bill_image_url VARCHAR(500),  -- ảnh bill (Firebase URL)
    status VARCHAR(20) DEFAULT 'PENDING',

    CONSTRAINT fk_subscription_mechanic
        FOREIGN KEY (mechanic_id)
        REFERENCES mechanics(mechanic_id)
        ON DELETE CASCADE
);
INSERT INTO services (name, icon_url, base_price) VALUES
('Dịch Vụ Kéo Xe', NULL, 0.00),
('Thay Lốp Xe', NULL, 0.00),
('Nổ Máy', NULL, 0.00),
('Giao Nhiên Liệu', NULL, 0.00),
('Mở Khóa Xe', NULL, 0.00),
('Sự Cố Cơ Khí', NULL, 0.00);