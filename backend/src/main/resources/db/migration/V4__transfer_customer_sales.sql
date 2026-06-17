CREATE TABLE customer (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    contact TEXT,
    phone TEXT,
    address TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE transfer (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    doc_no TEXT NOT NULL UNIQUE,
    from_warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    to_warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    operator_id INTEGER NOT NULL REFERENCES user_account(id),
    status TEXT NOT NULL DEFAULT 'COMPLETED',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE transfer_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transfer_id INTEGER NOT NULL REFERENCES transfer(id),
    product_id INTEGER NOT NULL REFERENCES product(id),
    batch_id INTEGER NOT NULL REFERENCES product_batch(id),
    quantity REAL NOT NULL
);

CREATE TABLE sales_order (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    doc_no TEXT NOT NULL UNIQUE,
    customer_id INTEGER NOT NULL REFERENCES customer(id),
    warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    operator_id INTEGER NOT NULL REFERENCES user_account(id),
    total_amount REAL NOT NULL,
    status TEXT NOT NULL DEFAULT 'COMPLETED',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE sales_order_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sales_order_id INTEGER NOT NULL REFERENCES sales_order(id),
    product_id INTEGER NOT NULL REFERENCES product(id),
    batch_id INTEGER NOT NULL REFERENCES product_batch(id),
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    amount REAL NOT NULL
);
