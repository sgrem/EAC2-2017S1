package cat.xtec.ioc.eac2_2017s1;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 05/10/2017.
 */

public class MarcaXmlParser {

    // No fem servir namespaces
    private static final String ns = null;

    //Aquesta classe representa una entrada de noticia del RSS Feed
    public static class Entrada {
        public final String titol;          //Títol de la noticia
        public final String enllac;         //Enllaç a la noticia completa
        public final String autor;          //Autor de la notícia
        public final String descripcio;     //Descripció de la noticia
        public final String data;           //Data de publicació de la noticia
        public final String categoria;      //Categoria de la noticia
        public final String thumbnail;      //Enllaç al thumbnail de la imatge


        private Entrada(String title, String author, String link, String desc, String date, String cat, String thumb) {
            this.titol = title;
            this.autor = author;
            this.enllac = link;
            this.descripcio = desc;
            this.data = date;
            this.categoria = cat;
            this.thumbnail = thumb;
        }
    }

    public List<Entrada> analitza(InputStream in) throws XmlPullParserException, IOException {
        try {
            //Obtenim analitzador
            XmlPullParser parser = Xml.newPullParser();

            //No fem servir namespaces
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

            //Especifica l'entrada de l'analitzador
            parser.setInput(in, null);

            //Obtenim la primera etiqueta
            parser.nextTag();

            //Retornem la llista de noticies
            return llegirNoticies(parser);
        } finally {
            in.close();
        }
    }

    //Llegeix una llista de noticies de Marca a partir del parser i retorna una llista d'Entrades
    private List<Entrada> llegirNoticies(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Entrada> llistaEntrades = new ArrayList<Entrada>();

        //Comprova si l'event actual és del tipus esperat (START_TAG) i del nom "feed"
        parser.require(XmlPullParser.START_TAG, ns, "feed");

        //Mentre que no arribem al final d'etiqueta
        while (parser.next() != XmlPullParser.END_TAG) {
            //Ignorem tots els events que no siguin un començament d'etiqueta
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                //Saltem al seguent event
                continue;
            }

            //Obtenim el nom de l'etiqueta
            String name = parser.getName();

            // Si aquesta etiqueta és una entrada de noticia
            if (name.equals("entry")) {
                //Afegim l'entrada a la llista
                llistaEntrades.add(llegirEntrada(parser));
            } else {
                //Si és una altra cosa la saltem
                saltar(parser);
            }
        }
        return llistaEntrades;
    }

    //Analitza el contingut d'una entrada. Si troba un ttol, resum o enllaç, crida els mètodes de lectura
    //propis per processar-los. Si no, ignora l'etiqueta.
    private Entrada llegirEntrada(XmlPullParser parser) throws XmlPullParserException, IOException {
        String titol = null;
        String enllac = null;
        String autor = null;
        String descripcio = null;
        String data = null;
        String categoria = null;
        String thumbnail = null;

        //L'etiqueta actual ha de ser "entry"
        parser.require(XmlPullParser.START_TAG, ns, "entry");

        //Mentre que no acabe l'etiqueta de "entry"
        while (parser.next() != XmlPullParser.END_TAG) {
            //Ignora fins que no trobem un començament d'etiqueta
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            //Obtenim el nom de l'etiqueta
            String etiqueta = parser.getName();

            switch (etiqueta) {
                case "title":
                    titol = llegirTitol(parser);
                    break;
                case "enllac":
                    enllac = llegirEnllac(parser);
                    break;
                case "creator":
                    autor = llegirAutor(parser);
                    break;
                case "description":
                    descripcio = llegirDescripcio(parser);
                    break;
                case "pubDate":
                    data = llegirData(parser);
                    break;
                case "category":
                    categoria = llegirCategoria(parser);
                    break;
                case "thumbnail":
                    thumbnail = llegirThumbnail(parser);
                    break;
                default:
                    saltar(parser);
            }
        }

        //Creem una nova entrada amb aquestes dades i la retornem
        return new Entrada(titol, enllac, autor, descripcio, data, categoria, thumbnail);
    }

    //Aquesta funció serveix per saltar-se una etiqueta i les seves subetiquetes aniuades.
    private void saltar(XmlPullParser parser) throws XmlPullParserException, IOException {
        //Si no és un començament d'etiqueta: ERROR
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;

        //Comprova que ha passat per tantes etiquetes de començament com acabament d'etiqueta

        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    //Cada vegada que es tanca una etiqueta resta 1
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    //Cada vegada que s'obre una etiqueta augmenta 1
                    depth++;
                    break;
            }
        }
    }

    //Llegeix el títol de una notcia del feed i el retorna com String
    private String llegirTitol(XmlPullParser parser) throws IOException, XmlPullParserException {
        //L'etiqueta actual ha de ser "title"
        parser.require(XmlPullParser.START_TAG, ns, "title");

        //Llegeix
        String titol = llegeixText(parser);

        //Fi d'etiqueta
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return titol;
    }

    //Llegeix l'enllaç de una notícia del feed i el retorna com String
    private String llegirEnllac(XmlPullParser parser) throws IOException, XmlPullParserException {
        String enllac = "";

        //L'etiqueta actual ha de ser "link"
        parser.require(XmlPullParser.START_TAG, ns, "link");

        //Obtenim l'etiqueta
        String tag = parser.getName();

        //Obtenim l'atribut rel (mirar l'XML d'Stackoverflow)
        String relType = parser.getAttributeValue(null, "rel");

        //Si l'enllaç és link

        if (tag.equals("link")) {
            //Obtenim l'enlla del valor de l'atribut "href". Revisar format XML stackoverflow
            if (relType.equals("alternate")) {
                enllac = parser.getAttributeValue(null, "href");
                parser.nextTag();
            }
        }

        //Fi d'etiqueta
        parser.require(XmlPullParser.END_TAG, ns, "link");

        return enllac;
    }

    //Llegeix l'autor de una notícia del feed i el retorna com String
    private String llegirAutor(XmlPullParser parser) throws IOException, XmlPullParserException {
        //L'etiqueta actual ha de ser "summary"
        parser.require(XmlPullParser.START_TAG, ns, "creator");

        String resum = llegeixText(parser);

        parser.require(XmlPullParser.END_TAG, ns, "creator");
        return resum;
    }

    //Llegeix la descripció de una notícia del feed i el retorna com String
    private String llegirDescripcio(XmlPullParser parser) throws IOException, XmlPullParserException {
        //L'etiqueta actual ha de ser "summary"
        parser.require(XmlPullParser.START_TAG, ns, "description");

        String resum = llegeixText(parser);

        parser.require(XmlPullParser.END_TAG, ns, "description");
        return resum;
    }

    //Llegeix la data de publicació de una notícia del feed i el retorna com String
    private String llegirData(XmlPullParser parser) throws IOException, XmlPullParserException {
        //L'etiqueta actual ha de ser "summary"
        parser.require(XmlPullParser.START_TAG, ns, "pubDate");

        String resum = llegeixText(parser);

        parser.require(XmlPullParser.END_TAG, ns, "pubDate");
        return resum;
    }

    //Llegeix la categoría de una notícia del feed i el retorna com String
    private String llegirCategoria(XmlPullParser parser) throws IOException, XmlPullParserException {
        //L'etiqueta actual ha de ser "summary"
        parser.require(XmlPullParser.START_TAG, ns, "category");

        String resum = llegeixText(parser);

        parser.require(XmlPullParser.END_TAG, ns, "category");
        return resum;
    }

    //Llegeix l'enllaç al thumbnail de l'imatge de una notícia del feed i el retorna com String
    private String llegirThumbnail(XmlPullParser parser) throws IOException, XmlPullParserException {

        String thumbnail = "";

        //L'etiqueta actual ha de ser "thumbnail"
        parser.require(XmlPullParser.START_TAG, ns, "thumbnail ");

        //Obtenim l'etiqueta
        String tag = parser.getName();

        //Obtenim l'atribut url
        String relType = parser.getAttributeValue(null, "url");

        //Si l'enllaç és link
        if (tag.equals("link")) {
            //Obtenim l'enllaç del valor de l'atribut "url".
            if (relType.equals("alternate")) {
                thumbnail = parser.getAttributeValue(null, "url");
                parser.nextTag();
            }
        }
        return thumbnail;
    }



    //Extrau el valor de text per les etiquetes
    private String llegeixText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String resultat = "";

        if (parser.next() == XmlPullParser.TEXT) {
            resultat = parser.getText();
            parser.nextTag();
        }
        return resultat;
    }


}