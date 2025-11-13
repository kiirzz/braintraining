#!/usr/bin/env python3
import tomlkit
from pathlib import Path

TOML_FILE = Path(__file__).parent / "libs.versions.toml"

def sort_table(table):
    sorted_items = sorted(table.items(), key=lambda kv: kv[0])
    new_table = tomlkit.table()
    new_table.update(sorted_items)
    return new_table

def format_toml_file(path: Path):
    if not path.exists():
        print(f"File not found: {path}")
        return

    with path.open("r", encoding="utf-8") as f:
        content = f.read()

    doc = tomlkit.parse(content)

    for key, table in doc.items():
        if isinstance(table, tomlkit.items.Table):
            doc[key] = sort_table(table)

    formatted = tomlkit.dumps(doc).strip() + "\n"

    with path.open("w", encoding="utf-8") as f:
        f.write(formatted)

    print(f"Formatted {path} successfully!")

if __name__ == "__main__":
    format_toml_file(TOML_FILE)
