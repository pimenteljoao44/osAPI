#!/usr/bin/env python3
"""
Migracao mecanica: injecao por campo (@Autowired) -> injecao por construtor
(campos final + Lombok @RequiredArgsConstructor).

Porte em Python do tools/migrate-constructor-injection.ps1 original (mesma
logica, adaptado do PowerShell para rodar em qualquer SO sem depender de
pwsh). Regras:

  1. Linha standalone "@Autowired" e removida; o campo seguinte ganha "final".
  2. Se o "@Autowired" preceder algo que nao e um campo simples, a linha e
     mantida e o arquivo entra na lista de revisao manual.
  3. Se algum campo virou final, a classe ganha @RequiredArgsConstructor e o
     import correspondente; o import de @Autowired cai se nao restar uso.
  4. Arquivos com construtor explicito (public/protected NomeDaClasse() {)
     sao PULADOS (revisao manual) para nao conflitar com o Lombok.

Uso: python3 migrate-constructor-injection.py [root_dir]
"""
import re
import sys
from pathlib import Path

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else "src/main/java")

FIELD_RE = re.compile(r'^(\s*)(private|protected|public)(\s+)(?!final)(\S.*;\s*)$')
AUTOWIRED_RE = re.compile(r'^\s*@Autowired\s*$')
CLASS_RE = re.compile(r'^(public\s+|final\s+)*class\s+\w')
PACKAGE_RE = re.compile(r'^package\s')
AUTOWIRED_IMPORT_RE = re.compile(r'^import\s+org\.springframework\.beans\.factory\.annotation\.Autowired;\s*$')

changed = []
manual_review = []

for path in sorted(ROOT.rglob("*.java")):
    text = path.read_text(encoding="utf-8")
    if "@Autowired" not in text:
        continue

    class_name = path.stem
    ctor_re = re.compile(rf'(public|protected)\s+{re.escape(class_name)}\s*\(')
    if ctor_re.search(text):
        manual_review.append(str(path))
        continue

    lines = text.splitlines()
    out = []
    pending = False
    made_final = 0

    for line in lines:
        if AUTOWIRED_RE.match(line):
            pending = True
            continue

        if pending:
            m = FIELD_RE.match(line)
            if m:
                out.append(f"{m.group(1)}{m.group(2)}{m.group(3)}final {m.group(4)}")
                made_final += 1
                pending = False
                continue
            elif line.strip() == "":
                out.append(line)
                continue
            else:
                out.append(line)
                pending = False
                manual_review.append(f"{path} (padrao inesperado apos @Autowired)")
                continue
        out.append(line)

    if made_final > 0:
        for i, l in enumerate(out):
            if CLASS_RE.match(l):
                joined = "\n".join(out)
                if "@RequiredArgsConstructor" not in joined:
                    out.insert(i, "@RequiredArgsConstructor")
                break
        joined = "\n".join(out)
        if "import lombok.RequiredArgsConstructor;" not in joined:
            for i, l in enumerate(out):
                if PACKAGE_RE.match(l):
                    out.insert(i + 1, "")
                    out.insert(i + 2, "import lombok.RequiredArgsConstructor;")
                    break

    joined = "\n".join(out)
    if "@Autowired" not in joined:
        out = [l for l in out if not AUTOWIRED_IMPORT_RE.match(l)]

    new_text = "\n".join(out) + "\n"
    if new_text != text:
        path.write_text(new_text, encoding="utf-8")
        changed.append(str(path))

print(f"=== ALTERADOS ({len(changed)}) ===")
for c in changed:
    print(c.replace(str(ROOT) + "/com/joao/osMarmoraria/", ""))

print()
print(f"=== REVISAO MANUAL ({len(set(manual_review))}) ===")
for m in sorted(set(manual_review)):
    print(m.replace(str(ROOT) + "/com/joao/osMarmoraria/", ""))
