CREATE TABLE role (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE warehouse (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    address TEXT,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE user_account (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    display_name TEXT NOT NULL,
    role_id INTEGER NOT NULL REFERENCES role(id),
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE user_warehouse (
    user_id INTEGER NOT NULL REFERENCES user_account(id),
    warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    PRIMARY KEY (user_id, warehouse_id)
);

CREATE TABLE product_category (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    parent_id INTEGER REFERENCES product_category(id)
);

CREATE TABLE product (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    sku TEXT NOT NULL UNIQUE,
    category_id INTEGER REFERENCES product_category(id),
    cost_price REAL NOT NULL DEFAULT 0,
    sale_price REAL NOT NULL DEFAULT 0,
    unit TEXT NOT NULL DEFAULT '件',
    min_stock REAL NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE product_batch (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL REFERENCES product(id),
    batch_no TEXT NOT NULL,
    production_date TEXT,
    UNIQUE(product_id, batch_no)
);

CREATE TABLE stock (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    product_id INTEGER NOT NULL REFERENCES product(id),
    batch_id INTEGER NOT NULL REFERENCES product_batch(id),
    quantity REAL NOT NULL DEFAULT 0,
    UNIQUE(warehouse_id, product_id, batch_id)
);

CREATE TABLE stock_in (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    doc_no TEXT NOT NULL UNIQUE,
    warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    operator_id INTEGER NOT NULL REFERENCES user_account(id),
    remark TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE stock_in_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    stock_in_id INTEGER NOT NULL REFERENCES stock_in(id),
    product_id INTEGER NOT NULL REFERENCES product(id),
    batch_id INTEGER NOT NULL REFERENCES product_batch(id),
    quantity REAL NOT NULL
);

CREATE TABLE stock_out (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    doc_no TEXT NOT NULL UNIQUE,
    warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    operator_id INTEGER NOT NULL REFERENCES user_account(id),
    out_type TEXT NOT NULL,
    remark TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE stock_out_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    stock_out_id INTEGER NOT NULL REFERENCES stock_out(id),
    product_id INTEGER NOT NULL REFERENCES product(id),
    batch_id INTEGER NOT NULL REFERENCES product_batch(id),
    quantity REAL NOT NULL
);

CREATE TABLE stock_check (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    doc_no TEXT NOT NULL UNIQUE,
    warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    operator_id INTEGER NOT NULL REFERENCES user_account(id),
    remark TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE stock_check_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    stock_check_id INTEGER NOT NULL REFERENCES stock_check(id),
    product_id INTEGER NOT NULL REFERENCES product(id),
    batch_id INTEGER NOT NULL REFERENCES product_batch(id),
    book_qty REAL NOT NULL,
    actual_qty REAL NOT NULL,
    diff_qty REAL NOT NULL
);

INSERT INTO role (name) VALUES ('admin'), ('warehouse_sales'), ('finance');
