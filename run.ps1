# javac Web\Server.java; if ($?) {java -cp Web Server}

$cp = ".;out;lib/*"
New-Item -ItemType Directory -Force -Path "out" | Out-Null

Get-ChildItem -Path "handler", "services" -Filter *.java |
    Resolve-Path -Relative |
    ForEach-Object { $_ -replace '^\.\\', '' } |
    Set-Content -Path "sources.txt" -Encoding Ascii

Add-Content -Path "sources.txt" -Value "DB\UserDao.java" -Encoding Ascii
Add-Content -Path "sources.txt" -Value "Server.java" -Encoding Ascii

javac -cp $cp -d out "@sources.txt"

if ($?) {
    java -cp $cp Server
}