## TsonConfigurations

### Преобразования типов и TsonObj
TsonObj - базовый элемент TsonConfigurations
Поддерживаемые типы данных:
- TsonString `"data"` или `'data'`
- TsonPrimitive:
  - TsonBool `(true)` или `(false)`
  - TsonClass `(java.lang.String)` и.др
  - TsonInt `(10)`
  - TsonFloat и TsonDouble `(10.5)`
- TsonList `[item1, item2, ... item-last]`
- TsonMap `{key=value, ... key-last=value-last}`
- TsonField ` <(class), constructor-params> `
```java
TsonObj obj = ...
//проверить, тип данных
if(obj.isNumber()){
    int integer = obj.getInt();
} else if (obj.isMap()){
    TsonList list = obj.getMap().getList("key");
}
```

### Коллекции
```java
TsonMap map = new TsonMap("{key='value'}");
TsonList list = new TsonList("['value1','value2']");
```
### Обьекты
```java
class Example implements TsonSerelizable{
    private final String k;
    private final String v;
    public Example(String k, String v){
        this.k = k;this.v = v;
    }
    
    public Example(TsonMap mp){
        k = mp.getStr("k");
        v = mp.getStr("v");
    }
    
    @Override
    public TsonObj toTson() {
        TsonMap mp = new TsonMap();
        mp.put("k", k);
        mp.put("v",v);
        return mp;
    }
}
```


```java
TsonField<Example> filed = new TsonField<>(
        "'<(Example), 'k1', 'v1'>'"
);
Example exm = field.getFiled();
//эквивалентно вызову new Example("k1", "v1")
//TsonField при чтении из строки вызывает
//new Example(tsonObj);
```

### Примитивы, классы
Tson считает за примимитивы:
- Числа (Integer), (Float), (Double)
- Логические (true), (false)
- Все имена классов (java.lang.String) и т.д
```java
TsonPrimitive primitive = TsonPrimitive.build("(10)");
```
### Преобразования
```java
String strMap = "{key='value'}";
TsonMap map = new TsonMap(strMap);
//преобразование в строку, из которой можно восстановить объект
strMap = map.toString();
//преобразование в JSON строку. Использование TsonField не возможно.
String jsonString = map.toJsonStr();
//{"key": "value"}
```
### Безопасность, ClassManager
При необходимости ограничить список классов, объекты которых
могут быть порождены `TsonField`, необходимо передать собственный `ClassManager`
в конструктор Tson компонента. Все дочерние объекты так же будут пораждены с использованием данного `ClassManager`. 
```java
ClassManager manager = new ClassManager(){
    @Override
    public Class<?> forName(String clazz) {
        switch (clazz){
            case "#": return Example.class;
            case ...
        }
        throw new NoSearchException(clazz);
    }
};

Example example = new TsonField<Example>(manager, "<(#), 'k1', 'v1'>").getField();
```
### TsonFile и TsonParser
`TsonParser` - обработчик синтаксиса Tson. Он же и отвечает за декодирование обьектов.
`TsonParser` допускает наличие произвольных символов, включая `\t`, `\r`.
По этой причине рекомендуется использовать `TsonFile` для считывания Tson из файла, который удаляет нежелательные символы:
```java
new TsonFile("file.tson")
//или
new TsonFile(new File("file.tson")).load()
```