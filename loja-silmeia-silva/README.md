# Site da loja Silmeia Silva

Site de uma página para a loja [@silmeiasilvaoficial](https://www.instagram.com/silmeiasilvaoficial/), com visual inspirado no site da Tesla: seções em tela cheia, botões grandes e um configurador para escolher modelo, cor e tamanho.

Os pedidos são finalizados pelo WhatsApp. A sacola monta uma mensagem com os itens e o total.

## Como abrir

Abra `index.html` no navegador. Não precisa de servidor nem de instalação.

## O que editar

Tudo fica no bloco `CONFIGURAÇÃO DA LOJA`, no começo do `<script>` do `index.html`:

- `LOJA.whatsapp`: número com DDI e DDD, só dígitos (ex.: `5511999998888`). **Troque antes de publicar.**
- `LOJA.info`: textos de atendimento, pagamento e entrega.
- `CATEGORIAS`: nome, frase e cor do texto (`tom`) de cada seção.
- `PRODUTOS`: nome, preço, descrição, cores (nome e cor em hex) e tamanhos. Os produtos que vêm no arquivo são exemplos e precisam ser trocados pelas peças reais.

## Fotos

Coloque as fotos na pasta `img/` com estes nomes e elas aparecem sozinhas como fundo das seções:

| Arquivo | Seção |
| --- | --- |
| `img/capa.jpg` | Abertura |
| `img/vestidos.jpg` | Vestidos |
| `img/conjuntos.jpg` | Conjuntos |
| `img/blusas.jpg` | Blusas |
| `img/acessorios.jpg` | Acessórios |
| `img/loja.jpg` | Fale com a loja |

Use fotos na horizontal com pelo menos 1920 px de largura. Sem foto, a seção mostra um fundo de tecido com a silhueta da peça.

Para usar foto num produto, adicione `foto: "img/nome-da-foto.jpg"` ao produto. Sem foto, o configurador desenha a silhueta da peça na cor escolhida.

## Publicar

O site é um arquivo estático. Funciona no GitHub Pages, Netlify ou Vercel enviando a pasta `loja-silmeia-silva/` inteira.
