# Spec

[fu.drawio](fu.drawio)

在样式内部添加字符，新的字符自动继承样式。如果是样式的边缘，默认不会，这意味着需要我们自行处理。

RichEditText 会通过**cursorStyle** 提示当前光标所在位置的样式,即在此处输入新的字符将会继承的样式。

如果用户反选当前光标，会插入一个特殊的Span，名为**Break**，并且在输入有效的字符后会被删除。如果Break
被一种样式包裹，除了前面的处理外，还会分割此样式。

```mermaid
flowchart LR
    classDef orange fill:#f96
    A[Start] --> B{是否包含样式}
    B -->|Yes| C{"`是否与样式
    起始终点
    对齐`"}
    C --> |yes| D[与样式对齐]
    C --> |no| E{"`是否处在
    完整样式
    的中间`"}
    B --> |no| H[不包含样式]
    E --> |yes| G[处于完整样式的中间]
    E --> |no| F[没有处在完整样式的中间]
    f>"替换此处的内容不会继承样式"]:::orange --- F
    f1>"添加样式会移除原有的样式并添加更长范围的新的样式"] --- F
    g>"替换此处的内容会继承样式"]:::orange --- G
    g1>"取消样式会输入一个Break，知道输入有效字符后才删除"] --- G
    g --- D
    f --- H
```

Break 取消是还需要判断是否需要合并前后的样式。

新插入的字符会继承前面的样式。