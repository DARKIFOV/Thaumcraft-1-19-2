from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

# Java allows only these single-character escapes in normal string literals.
# Regex escapes such as \d or \.' must therefore be written as "\\d" / "\\.".
VALID_SINGLE_ESCAPES = set('btnfr"\'\\')


def strip_comments_preserve_strings(text: str) -> str:
    out = []
    i = 0
    n = len(text)
    in_string = False
    in_char = False
    while i < n:
        ch = text[i]
        nxt = text[i + 1] if i + 1 < n else ''
        if in_string:
            out.append(ch)
            if ch == '\\' and i + 1 < n:
                out.append(text[i + 1])
                i += 2
                continue
            if ch == '"':
                in_string = False
            i += 1
            continue
        if in_char:
            out.append(' ')
            if ch == '\\' and i + 1 < n:
                out.append(' ')
                i += 2
                continue
            if ch == "'":
                in_char = False
            i += 1
            continue
        if ch == '"':
            in_string = True
            out.append(ch)
            i += 1
            continue
        if ch == "'":
            in_char = True
            out.append(' ')
            i += 1
            continue
        if ch == '/' and nxt == '/':
            while i < n and text[i] != '\n':
                out.append(' ')
                i += 1
            continue
        if ch == '/' and nxt == '*':
            out.extend('  ')
            i += 2
            while i < n and not (text[i] == '*' and i + 1 < n and text[i + 1] == '/'):
                out.append('\n' if text[i] == '\n' else ' ')
                i += 1
            if i < n:
                out.extend('  ')
                i += 2
            continue
        out.append(ch)
        i += 1
    return ''.join(out)


def find_illegal_string_escapes(text: str):
    clean = strip_comments_preserve_strings(text)
    i = 0
    n = len(clean)
    while i < n:
        if clean[i] != '"':
            i += 1
            continue
        start = i
        i += 1
        while i < n:
            ch = clean[i]
            if ch == '"':
                i += 1
                break
            if ch == '\\':
                if i + 1 >= n:
                    yield start, i, '<eof>'
                    i += 1
                    continue
                esc = clean[i + 1]
                if esc == 'u':
                    j = i + 2
                    while j < n and clean[j] == 'u':
                        j += 1
                    hex_digits = clean[j:j + 4]
                    if len(hex_digits) < 4 or any(c not in '0123456789abcdefABCDEF' for c in hex_digits):
                        yield start, i, '\\u' + hex_digits
                        i += 2
                    else:
                        i = j + 4
                    continue
                if esc not in VALID_SINGLE_ESCAPES and esc not in '01234567':
                    yield start, i, '\\' + esc
                i += 2
                continue
            i += 1



def find_delimiter_errors(text: str):
    """Find unmatched (), [] and {} outside comments/string/char literals."""
    stack = []
    pairs = {')': '(', ']': '[', '}': '{'}
    openers = set(pairs.values())
    i = 0
    n = len(text)
    state = 'code'
    while i < n:
        ch = text[i]
        nxt = text[i + 1] if i + 1 < n else ''
        if state == 'line_comment':
            if ch == '\n':
                state = 'code'
            i += 1
            continue
        if state == 'block_comment':
            if ch == '*' and nxt == '/':
                state = 'code'
                i += 2
            else:
                i += 1
            continue
        if state == 'string':
            if ch == '\\' and i + 1 < n:
                i += 2
            else:
                if ch == '"':
                    state = 'code'
                i += 1
            continue
        if state == 'char':
            if ch == '\\' and i + 1 < n:
                i += 2
            else:
                if ch == "'":
                    state = 'code'
                i += 1
            continue
        if ch == '/' and nxt == '/':
            state = 'line_comment'
            i += 2
            continue
        if ch == '/' and nxt == '*':
            state = 'block_comment'
            i += 2
            continue
        if ch == '"':
            state = 'string'
            i += 1
            continue
        if ch == "'":
            state = 'char'
            i += 1
            continue
        if ch in openers:
            stack.append((ch, i))
        elif ch in pairs:
            if not stack:
                yield i, f'unexpected closing {ch}'
            else:
                opening, opening_index = stack.pop()
                if opening != pairs[ch]:
                    yield i, f'mismatched {opening} ... {ch}'
        i += 1
    for opening, index in reversed(stack):
        yield index, f'unclosed {opening}'

def line_col(text: str, index: int):
    line = text.count('\n', 0, index) + 1
    last_nl = text.rfind('\n', 0, index)
    col = index + 1 if last_nl == -1 else index - last_nl
    return line, col


for path in (ROOT / 'src/main/java').rglob('*.java'):
    text = path.read_text(encoding='utf-8', errors='ignore')
    rel = path.relative_to(ROOT)

    if '\\"' in text:
        errors.append(f'{rel}: contains literal escaped quote sequence \\\\" in Java source')

    # The project should not contain source-level backslash-n sequences used as physical newlines.
    # Allow this only if future code explicitly needs it inside a string by annotating the file.
    if '\\n        ' in text or '\\n    ' in text:
        errors.append(f'{rel}: contains literal backslash-n indentation sequence in Java source')

    for _string_start, escape_index, escape in find_illegal_string_escapes(text):
        line, col = line_col(text, escape_index)
        errors.append(f'{rel}:{line}:{col}: illegal Java string escape {escape!r}; double regex backslashes, e.g. "\\\\d"')

    for delimiter_index, message in find_delimiter_errors(text):
        line, col = line_col(text, delimiter_index)
        errors.append(f'{rel}:{line}:{col}: {message}')

if errors:
    for error in errors:
        print('::error::' + error)
    sys.exit(1)

print('Java syntax guard: OK')
