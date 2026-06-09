# =============================================================================
# Migracao mecanica: injecao por campo (@Autowired) -> injecao por construtor
# (campos final + Lombok @RequiredArgsConstructor).
#
# Regras:
#   1. Linha standalone "@Autowired" e removida; o campo seguinte ganha "final".
#   2. Se o "@Autowired" preceder um construtor/metodo, apenas a anotacao cai
#      (Spring injeta no construtor unico sem anotacao desde a versao 4.3) e o
#      arquivo e listado para revisao manual.
#   3. Se algum campo virou final, a classe ganha @RequiredArgsConstructor e o
#      import correspondente; o import de @Autowired e removido se nao restar uso.
#   4. Arquivos com construtor explicito sao PULADOS (revisao manual) para nao
#      conflitar com o construtor gerado pelo Lombok.
#
# Leitura/escrita com UTF-8 SEM BOM explicito: os fontes tem acentuacao e o
# Get-Content/Set-Content do PowerShell 5.1 corromperia o encoding.
# =============================================================================
param(
    [string]$Root = "C:\Users\jaovi\OneDrive\Documentos\ERP\osAPI\src\main\java"
)

$utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$manualReview = @()
$changed = @()

Get-ChildItem $Root -Recurse -Filter *.java | ForEach-Object {
    $path = $_.FullName
    $text = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
    if ($text -notmatch '@Autowired') { return }

    $className = [System.IO.Path]::GetFileNameWithoutExtension($path)

    # Construtor explicito? (ex.: "public AuthController(") -> revisao manual
    if ($text -match "(public|protected)\s+$className\s*\(") {
        $manualReview += $path
        return
    }

    $lines = [System.Collections.Generic.List[string]]([System.IO.File]::ReadAllLines($path, [System.Text.Encoding]::UTF8))
    $out = [System.Collections.Generic.List[string]]::new()
    $pending = $false
    $madeFinal = 0

    foreach ($line in $lines) {
        if ($line -match '^\s*@Autowired\s*$') { $pending = $true; continue }

        if ($pending) {
            if ($line -match '^(\s*)(private|protected|public)(\s+)(?!final)(\S.*;\s*)$') {
                $out.Add("$($Matches[1])$($Matches[2])$($Matches[3])final $($Matches[4])")
                $madeFinal++
                $pending = $false
                continue
            }
            elseif ($line -match '^\s*$') {
                # linha em branco entre @Autowired e o campo: preserva e segue
                $out.Add($line); continue
            }
            else {
                # nao era um campo simples: mantem a linha e marca p/ revisao
                $out.Add($line); $pending = $false
                $manualReview += "$path (padrao inesperado apos @Autowired)"
                continue
            }
        }
        $out.Add($line)
    }

    if ($madeFinal -gt 0) {
        # injeta @RequiredArgsConstructor antes da declaracao da classe (1a ocorrencia)
        for ($i = 0; $i -lt $out.Count; $i++) {
            if ($out[$i] -match '^(public\s+|final\s+|abstract\s+)*class\s+\w') {
                if (($out -join "`n") -notmatch '@RequiredArgsConstructor') {
                    $out.Insert($i, '@RequiredArgsConstructor')
                }
                break
            }
        }
        # adiciona o import do lombok apos a linha de package (se ainda nao existir)
        if (($out -join "`n") -notmatch 'import\s+lombok\.RequiredArgsConstructor;') {
            for ($i = 0; $i -lt $out.Count; $i++) {
                if ($out[$i] -match '^package\s') {
                    $out.Insert($i + 1, ''); $out.Insert($i + 2, 'import lombok.RequiredArgsConstructor;')
                    break
                }
            }
        }
    }

    # remove o import de @Autowired se nao restar nenhum uso
    $joined = $out -join "`n"
    if ($joined -notmatch '@Autowired') {
        $out = [System.Collections.Generic.List[string]]($out | Where-Object { $_ -notmatch '^import\s+org\.springframework\.beans\.factory\.annotation\.Autowired;\s*$' })
    }

    [System.IO.File]::WriteAllLines($path, $out, $utf8NoBom)
    $changed += $path
}

"=== ALTERADOS ($($changed.Count)) ==="
$changed | ForEach-Object { $_.Replace("$Root\com\joao\osMarmoraria\", "") }
""
"=== REVISAO MANUAL ($($manualReview.Count)) ==="
$manualReview | Select-Object -Unique | ForEach-Object { $_.Replace("$Root\com\joao\osMarmoraria\", "") }
