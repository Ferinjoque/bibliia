package com.ferinjoque.university;

import java.io.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.util.*;
import java.text.Normalizer;
import java.util.stream.Collectors;

public class Main {
    private static Set<String> stopwords;

    public static void main(String[] args) {
        try {
            cargarStopwords("D:\\Users\\USER\\sistema-recomendacion-libros\\stopwords.txt");
            Scanner scanner = new Scanner(System.in);
            System.out.println("Ingrese la ruta del archivo Excel:");
            String archivoExcel = scanner.nextLine();
            if (archivoExcel.isEmpty()) {
                System.out.print("No se ha ingresado la ruta del archivo Excel. El programa se cerrará una vez presionado Enter.");
                scanner.nextLine();
                return;
            }
            File excelFile = new File(archivoExcel);
            if (!excelFile.exists() || !archivoExcel.toLowerCase().endsWith(".xlsx")) {
                System.out.print("El archivo Excel especificado no es válido. Asegúrese de proporcionar un archivo .xlsx existente. El programa se cerrará una vez presionado Enter.");
                scanner.nextLine();
                return;
            }

            System.out.println("Ingrese la ruta del archivo PDF:");
            String archivoPDF = scanner.nextLine();
            if (archivoPDF.isEmpty()) {
                System.out.print("No se ha ingresado la ruta del archivo PDF. El programa se cerrará una vez presionado Enter.");
                scanner.nextLine();
                return;
            }
            File pdfFile = new File(archivoPDF);
            if (!pdfFile.exists() || !archivoPDF.toLowerCase().endsWith(".pdf")) {
                System.out.print("El archivo PDF especificado no es válido. Asegúrese de proporcionar un archivo .pdf existente. El programa se cerrará una vez presionado Enter.");
                scanner.nextLine();
                return;
            }

            String archivoLibros = "D:\\Users\\USER\\sistema-recomendacion-libros\\base_datos_libros.txt";
            String nombreAlumno = obtenerNombreAlumno(archivoPDF);
            System.out.println();
            System.out.println("Bienvenido, " + nombreAlumno);
            System.out.println();
            procesarHistorialAcademico(archivoExcel, archivoPDF, archivoLibros);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            System.out.println();
            System.out.print("Presiona Enter para salir...");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para cargar las stopwords desde el archivo
    private static void cargarStopwords(String pathToStopwordsFile) throws IOException {
        stopwords = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader(pathToStopwordsFile));
        String linea;
        while ((linea = br.readLine()) != null) {
            stopwords.add(linea.trim());
        }
        br.close();
    }

    public static String obtenerNombreAlumno(String archivoPDF) throws IOException {
        try (PDDocument document = PDDocument.load(new File(archivoPDF))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String textoPlano = stripper.getText(document);
            String nombreAlumno = null;

            // Busca el nombre del alumno en el texto plano
            String[] lineas = textoPlano.split("\\r?\\n");
            for (String linea : lineas) {
                if (linea.startsWith("Alumno(a):")) {
                    nombreAlumno = linea.substring(10).trim(); // Elimina "Alumno(a): " del inicio
                    break;
                }
            }
            return nombreAlumno;
        }
    }
    public static void procesarHistorialAcademico(String archivoExcel, String archivoPDF, String archivoLibros) throws IOException {
        FileInputStream fis = new FileInputStream(archivoExcel);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(0);

        Map<String, String> cursosCompletados = new HashMap<>();
        Set<String> cursosEnCurso = new HashSet<>(); // Almacena los cursos que el usuario está cursando actualmente
        List<String> cursosConNota20 = new ArrayList<>(); // Almacena los nombres de los cursos con nota 20

        // Itera sobre las filas para leer los datos del historial académico
        for (Row row : sheet) {
            Cell estadoCell = row.getCell(7); // Suponiendo que el estado está en la octava columna
            if (estadoCell == null || estadoCell.getCellType() == CellType.BLANK) {
                continue; // Ignora las filas sin estado
            }

            String estado = estadoCell.getStringCellValue();
            String curso = row.getCell(2).getStringCellValue(); // Suponiendo que el nombre del curso está en la tercera columna

            if (curso.trim().isEmpty() || curso.equals("Nombre de Curso")) {
                continue; // Ignora filas con encabezados o información no válida
            }

            String codigoCurso = row.getCell(1).getStringCellValue(); // Suponiendo que el código del curso está en la segunda columna

            // Verifica el estado del curso
            if (estado.equals("APROBADO") || estado.equals("CONVALIDADO")) {
                cursosCompletados.put(codigoCurso, estado); // Almacena el estado (APROBADO o CONVALIDADO) como valor en lugar de cursos
            } else if (estado.equals("EN CURSO")) {
                cursosEnCurso.add(curso); // Agrega el curso a los cursos "En Curso"
                cursosCompletados.put(codigoCurso, estado); // Almacena el estado (EN CURSO) como valor en lugar de cursos
            }

            // Obtiene la nota del curso y verifica si es 20
            String nota = obtenerNota(codigoCurso, archivoPDF);
            if (nota != null && nota.equals("20")) {
                cursosConNota20.add(curso);
            }
        }
        fis.close();

        // Imprime la felicitación si hay cursos con nota 20
        if (!cursosConNota20.isEmpty()) {
            System.out.println("¡Felicidades por sacar 20 en los siguientes cursos!");
            for (String curso20 : cursosConNota20) {
                System.out.println(curso20);
            }
            System.out.println();
        }

        // Lee la base de datos de libros en texto plano
        List<Main.Libro> libros = leerBaseDatosLibros(archivoLibros);

        // Recomienda libros basados en los cursos "En Curso"
        recomendarLibros(libros, cursosEnCurso);
    }

    public static List<Main.Libro> leerBaseDatosLibros(String archivoLibros) throws IOException {
        List<Main.Libro> libros = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(archivoLibros));
        String titulo = null;
        String autor = null;
        String fecha = null;
        String linea;

        while ((linea = br.readLine()) != null) {
            if (!linea.trim().isEmpty()) {
                if (titulo == null) {
                    titulo = linea.trim();
                } else if (autor == null) {
                    autor = linea.trim().substring(7); // Quita "Autor: " del inicio de la línea
                } else if (linea.startsWith("Fecha de publicación:")) {
                    fecha = linea.trim().substring(21).trim(); // Quita "Fecha de publicación: " del inicio de la línea y elimina espacios en blanco adicionales
                    if (fecha.isEmpty()) {
                        fecha = "Sin Fecha"; // Si no hay fecha, establece "Sin Fecha"
                    }
                    libros.add(new Main.Libro(titulo, autor, fecha));
                    // Reinicia para el siguiente libro
                    titulo = null;
                    autor = null;
                    fecha = null;
                }
            }
        }
        br.close();
        return libros;
    }

    public static void recomendarLibros(List<Libro> libros, Set<String> cursosEnCurso) {
        boolean firstCourse = true;
        for (String curso : cursosEnCurso) {
            boolean cursoEncontrado = false;
            List<String> librosRecomendados = new ArrayList<>();

            for (Libro libro : libros) {
                // Enfoque basado en similitud de palabras clave
                if (contienePalabraClave(libro.getTitulo(), curso)) {
                    if (!cursoEncontrado) {
                        if (!firstCourse) {
                            System.out.println(); // Agrega un espacio si no es el primer curso
                        }
                        System.out.println("Según tu curso " + curso + ", se te recomiendan los siguientes libros:");
                        cursoEncontrado = true;
                    }
                    librosRecomendados.add(libro.getTitulo() + " - Autor: " + libro.getAutor() + " - Fecha de publicación: " + libro.getFecha());
                }
            }

            if (cursoEncontrado) {
                for (String libroRecomendado : librosRecomendados) {
                    System.out.println(libroRecomendado);
                }
            } else {
                System.out.println();
                System.out.println("No se encontraron libros recomendados para el curso: " + curso);
            }

            firstCourse = false;
        }
    }

    public static boolean contienePalabraClave(String texto, String palabraClave) {
        // Tokeniza y normaliza la palabra clave
        List<String> tokensClave = Arrays.stream(palabraClave.split("\\s+"))
                .map(token -> normalizarToken(token))
                .filter(token -> !stopwords.contains(token)) // Filtra stopwords
                .collect(Collectors.toList());

        // Tokeniza y normaliza el texto del libro, y filtra stopwords
        List<String> tokensTexto = Arrays.stream(texto.split("\\s+"))
                .map(token -> normalizarToken(token))
                .filter(token -> !stopwords.contains(token)) // Filtrar stopwords
                .collect(Collectors.toList());

        // Carga un diccionario de sinónimos y palabras relacionadas
        Map<String, List<String>> sinonimosRelacionados = new HashMap<>();
        // Llenado del diccionario
        sinonimosRelacionados.put("textos", Arrays.asList("redactar", "escribir", "lengua", "conectores", "español", "escrita"));
        sinonimosRelacionados.put("ambiente", Arrays.asList("ecologia", "biodiversidad", "planeta"));
        sinonimosRelacionados.put("quimica", Arrays.asList("compuestos", "nomenclatura"));
        sinonimosRelacionados.put("ingles", Arrays.asList("english", "learning"));
        sinonimosRelacionados.put("matematica", Arrays.asList("calculo", "funciones", "algebra", "geometria"));
        sinonimosRelacionados.put("fisica", Arrays.asList("calculo", "funciones"));
        sinonimosRelacionados.put("programacion", Arrays.asList("web", "software", "algoritmos", "tecnologia", "datos", "data", "ciberseguridad", "codificacion"));
        sinonimosRelacionados.put("ciudadania", Arrays.asList("sociedad", "cultura"));
        sinonimosRelacionados.put("investigacion", Arrays.asList("lengua", "conectores", "español", "escrita"));
        sinonimosRelacionados.put("empresas", Arrays.asList("negocio", "estrategico", "estrategica", "coaching", "liderazgo"));
        sinonimosRelacionados.put("comunicacion", Arrays.asList("estrategico", "coaching", "liderazgo"));
        sinonimosRelacionados.put("desarrollo", Arrays.asList("web", "software", "tecnologia", "tecnologias", "datos", "data", "ciberseguridad", "codificacion"));
        sinonimosRelacionados.put("software", Arrays.asList("tecnologia", "tecnologias", "datos", "data", "ciberseguridad", "codificacion"));
        sinonimosRelacionados.put("gestion", Arrays.asList("estrategico", "estrategica", "coaching", "liderazgo"));

        for (String tokenTexto : tokensTexto) {
            for (String tokenClave : tokensClave) {
                if (tokenTexto.equals(tokenClave) || contieneSinonimo(sinonimosRelacionados, tokenClave, tokenTexto)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String normalizarToken(String token) {
        return Normalizer.normalize(token.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    public static boolean contieneSinonimo(Map<String, List<String>> sinonimosRelacionados, String palabraClave, String token) {
        List<String> sinonimos = sinonimosRelacionados.getOrDefault(palabraClave, Collections.emptyList());
        for (String sinonimo : sinonimos) {
            // Normaliza el sinónimo para realizar una comparación adecuada
            String sinonimoNormalizado = normalizarToken(sinonimo);
            if (token.equals(sinonimoNormalizado)) {
                return true;
            }
        }
        return false;
    }

    public static String obtenerNota(String codigoCurso, String archivoPDF) throws IOException {
        try (PDDocument document = PDDocument.load(new File(archivoPDF))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String textoPlano = stripper.getText(document);
            BufferedReader br = new BufferedReader(new StringReader(textoPlano));
            String linea;

            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("CICLO")) {
                    continue; // Ignora la línea de encabezado
                }

                String[] partes = linea.split("\\s+");
                // La nota siempre son dos dígitos
                String regexNota = "\\b\\d{2}\\b";
                // El estado es una de las tres opciones dadas
                if (partes.length >= 8) {
                    String codigoCursoActual = partes[1];
                    if (codigoCursoActual.equals(codigoCurso)) {
                        StringBuilder notaBuilder = new StringBuilder();
                        for (int i = 2; i < partes.length; i++) {
                            if (partes[i].matches(regexNota)) {
                                notaBuilder.append(partes[i]);
                                break;
                            }
                        }
                        return notaBuilder.toString();
                    }
                }
            }
            br.close();
        }
        return null; // Si no se encuentra la nota para el curso
    }

    static class Libro {
        private String titulo;
        private String autor;
        private String fecha;

        public Libro(String titulo, String autor, String fecha) {
            this.titulo = titulo;
            this.autor = autor;
            this.fecha = fecha;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getAutor() {
            return autor;
        }

        public String getFecha() {
            return fecha;
        }
    }
}