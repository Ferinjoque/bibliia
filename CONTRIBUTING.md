# Contribuyendo a Bibliia

## Enviar parches

### Confirmar mensajes
Escribe mensajes de confirmación que tengan sentido. Explica qué y *por qué* cambias.
Escribe en tiempo presente.
Lee [esta guía](https://gist.github.com/robertpainsi/b632364184e70900af4ab688decf6f53) para más información.

### GitHub
Me gustaría animar a las personas a utilizar GitHub para crear _pull requests_.
Me ayudaría a revisar los parches, organizar ramas con etiquetas e hitos, y apoyar a otros a ver en qué se está trabajando.

### Correo electrónico
En caso de que GitHub no funcione o no puedas usarlo por algún otro motivo, puedes enviar un parche a `ferinjoque@gmail.com`.

### Reglas

* Al corregir un error, descríbelo y cómo lo soluciona tu parche.
* Al agregar una nueva función, agrega una descripción de la función y cómo debe usarse.
* Cada parche o _pull request_ solo debe contener modificaciones relacionadas.
* Ejecuta las pruebas y los formateadores de código antes de enviarlos.
* En un futuro, al cambiar la interfaz de usuario, te agradecería que pudieras agregar una captura de pantalla del antes y el después para comparar.
* Si corresponde, documenta cómo probar la funcionalidad.

## Errores conocidos

Toma en cuenta que esto es aparte de la sección `Impedimentos` de [README.md](README.md).

* En ciertos casos, al analizar el Record de Notas, Bibliia encuentra cursos con nombres tan largos que se extienden a más de una línea. Esto puede hacer que el sistema no pueda reconocer el modelo proporcionado y, por lo tanto, omita ese curso, considerándolo como información irrelevante. Una solución potencial, que aún está en desarrollo, consiste en evitar establecer un orden específico para que Bibliia lo siga. En su lugar, se está explorando la posibilidad de comenzar desde el final del registro para identificar elementos como `Estado` o `Nota`. Basándose en esta información, el sistema determinaría si se trata o no de un curso válido, evitando que se descarten cursos directamente.

## Funciones planeadas

1. Actualmente, las notas solo se utilizan para felicitar al usuario en caso de que haya obtenido una calificación de 20 en algún curso. En un principio, consideré agregar funcionalidades adicionales como recomendaciones de cursos electivos, un análisis general del rendimiento y otras ideas similares. Sin embargo, decidí descartarlas antes de la publicación inicial del proyecto. Mantuve la función allí por si en el futuro puedo agregar algo más valioso que mis ideas iniciales.
2. En el futuro, cuando haya adquirido más conocimientos en Java y programación en general, tengo la intención de desarrollar una interfaz gráfica de usuario (GUI) con JavaFX para mejorar la experiencia del usuario al utilizar Bibliia.
