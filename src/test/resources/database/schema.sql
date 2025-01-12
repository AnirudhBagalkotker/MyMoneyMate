-- Users table
CREATE TABLE IF NOT EXISTS users (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	username TEXT NOT NULL UNIQUE,
	password TEXT NOT NULL,
	email TEXT UNIQUE,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- Categories table for predefined transaction categories
CREATE TABLE IF NOT EXISTS categories (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL UNIQUE,
	type TEXT NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
	description TEXT
);
-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	user_id INTEGER NOT NULL,
	amount DECIMAL(10, 2) NOT NULL,
	category_id INTEGER NOT NULL,
	description TEXT,
	transaction_date DATE NOT NULL,
	type TEXT NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY (user_id) REFERENCES users(id),
	FOREIGN KEY (category_id) REFERENCES categories(id)
);
-- Budgets table
CREATE TABLE IF NOT EXISTS budgets (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	user_id INTEGER NOT NULL,
	category_id INTEGER NOT NULL,
	amount DECIMAL(10, 2) NOT NULL,
	period TEXT NOT NULL CHECK (
		period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')
	),
	start_date DATE NOT NULL,
	end_date DATE,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY (user_id) REFERENCES users(id),
	FOREIGN KEY (category_id) REFERENCES categories(id)
);
-- Insert default categories
INSERT
	OR IGNORE INTO categories (name, type, description)
VALUES ('Salary', 'INCOME', 'Regular employment income'),
	('Freelance', 'INCOME', 'Freelance work income'),
	('Investments', 'INCOME', 'Investment returns'),
	('Other Income', 'INCOME', 'Miscellaneous income'),
	('Food', 'EXPENSE', 'Food items'),
	('Groceries', 'EXPENSE', 'Household items'),
	('Rent', 'EXPENSE', 'Housing rent'),
	('Utilities', 'EXPENSE', 'Gas, Elec etc.'),
	('Transport', 'EXPENSE', 'Transport, fuel'),
	('Entertainment', 'EXPENSE', 'Movies, games'),
	('Healthcare', 'EXPENSE', 'Medical expenses'),
	('Shopping', 'EXPENSE', 'Shopping'),
	('Other Expenses', 'EXPENSE', 'Miscellaneous');