import sqlite3, os

SERVER = r'E:/JAVA联机/db_backups/jubensha_backup_2026-07-17.sqlite'
LOCAL  = r'E:/JAVA联机/jubensha - 11/jubensha/example_db.sqlite'

def stats(path, label):
    if not os.path.exists(path):
        print(f'{label}: FILE NOT FOUND')
        return {}
    size_kb = os.path.getsize(path)/1024
    print(f'{label}: {size_kb:.1f} KB')
    db = sqlite3.connect(path)
    cur = db.cursor()
    cur.execute("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name")
    tables = [r[0] for r in cur.fetchall()]
    rows = {}
    for t in tables:
        try:
            cur.execute(f'SELECT COUNT(*) FROM "{t}"')
            rows[t] = cur.fetchone()[0]
        except:
            rows[t] = -1
    db.close()
    return rows

server_rows = stats(SERVER, '\n=== SERVER DB ===')
print()
local_rows  = stats(LOCAL,  '\n=== LOCAL DB ===')

all_tables = sorted(set(list(server_rows.keys()) + list(local_rows.keys())))
server_total = sum(v for v in server_rows.values() if v>0)
local_total  = sum(v for v in local_rows.values() if v>0)

print()
print('='*70)
print(f'{"Table":<30} {"Server":>8} {"Local":>8} {"Diff":>8}')
print('-'*58)
for t in all_tables:
    s = server_rows.get(t, 0)
    l = local_rows.get(t, 0)
    diff = s - l
    marker = ' ***' if diff != 0 else ''
    print(f'{t:<30} {s:>8} {l:>8} {diff:>+8}{marker}')
print('-'*58)
print(f'{"TOTAL ROWS":<30} {server_total:>8} {local_total:>8} {server_total-local_total:>+8}')

if server_total > local_total:
    print(f'\n>>> SERVER has {server_total - local_total} more rows')
elif local_total > server_total:
    print(f'\n>>> LOCAL has {local_total - server_total} more rows')
else:
    print('\n>>> Both have same row count')
