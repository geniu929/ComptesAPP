package com.example.eugen.comptesapp;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;



public class accueil extends Activity{
    Button add = null;
    Button generate = null;
    TextView groups = null;
    TextView pseudo = null;
    TextView dcnx = null;
    EditText ngp = null;
    ListView test = null;
    TextView list = null;
    EditText namegroup = null;
    Button acceder = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accueil); //récuperation du layout de la page depuis le fichier XML
        /* Récupération des boutons de l'activité depuis le fichier XML correspondant */
        generate = (Button) findViewById(R.id.generate);
        generate.setOnClickListener(generateListener);
        add = (Button) findViewById(R.id.add);
        groups = (TextView) findViewById(R.id.list);
        pseudo = (TextView) findViewById(R.id.nom);
        dcnx = (TextView) findViewById(R.id.dcnx);
        ngp = (EditText) findViewById(R.id.ngp);
        dcnx.setOnClickListener(deconnexionListener);
        list = (TextView) findViewById(R.id.list);
        test = (ListView) findViewById(R.id.test);
        acceder = (Button) findViewById(R.id.acceder);
        namegroup = (EditText) findViewById(R.id.namegroup);

        final String res = getIntent().getStringExtra("nom"); // Recupération de la valeur envoyé depuis l'activité précédente

        final SQLiteDatabase db = openOrCreateDatabase("projet", Context.MODE_PRIVATE, null);
        /* Récupération de l'id de l'utilisateur */
        final Cursor d = db.rawQuery("SELECT * from utilisateurs WHERE pseudo='" + res + "'", null);
        d.moveToFirst();
        final String id_u = d.getString(0);

        pseudo.setText("Bonjour, " + res +"  "); // Modification de l'entéte

        /* Masquer le bouton d'ajout et le champ du nom pour le moment (ils seront affichés quand l'utilsateur décide de créer un groupe */
        add.setVisibility(View.GONE);
        ngp.setVisibility(View.GONE);

        /* Création de la liste qui contient le nom des groupes et son Adaptateur */
        final ListView lv = (ListView) findViewById(R.id.test);
        final List<String> mesgroupes = new ArrayList<String>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, mesgroupes);
        lv.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

        db.execSQL("CREATE TABLE IF NOT EXISTS groupe(id INTEGER PRIMARY KEY AUTOINCREMENT ,nomg TEXT );"); //création de la table groupe
        db.execSQL("CREATE TABLE IF NOT EXISTS EstMembre( id_u INTEGER , id_g INTEGER , admin INTEGER);"); //Création de la table EstMembre

        /* Parcourt de la table groupe et l'ajout de son contenu dans la liste*/
        Cursor c =db.rawQuery("SELECT nomg FROM groupe ", null);
        while(c.moveToNext()) {
            mesgroupes.add(c.getString(0));
            arrayAdapter.notifyDataSetChanged();
        }
        arrayAdapter.notifyDataSetChanged();

        /* Méthode qui définit le comportement de l'application quand on tape le nom d'un groupe pour y accéder */
        OnClickListener gogroupListener = null;
        gogroupListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Iterator<String> i = mesgroupes.iterator(); //parcour de la liste qui contient les groupes
                Boolean bool = true;
                while(i.hasNext()) {
                    String ch = (String) i.next();
                    if (namegroup.getText().toString().equals(ch)) { //si on trouve le groupe dont l'utilisateur tente d'y accéder
                        bool = false;
                        Intent my1 = new Intent(accueil.this, group.class); //On accéde à la page du groupe
                        my1.putExtra("nom_u", res); //On envoie le nom de l'utilsateur actuel
                        my1.putExtra("id_u", id_u); //et son id
                        my1.putExtra("nom_g", namegroup.getText().toString()); // et le nom du groupe
                        startActivity(my1); //exécution
                    }
                }
                if (bool){ // Si on trouve pas le groupe
                    Toast.makeText(getApplicationContext(), "Groupe non trouvé", Toast.LENGTH_SHORT).show();
                }
            }

        };
        acceder.setOnClickListener(gogroupListener);

        /* Méthode qui définit le comportement de l'application quand on essaie d'ajouter un groupe */
        OnClickListener addListener = null;
        addListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor c = db.rawQuery("SELECT nomg FROM groupe ", null);
                int a =0;
                while (c.moveToNext()) {
                    if (c.getString(0).equals(ngp.getText().toString())) {
                        a++;
                    }
                }
                if (a==0){ // Si le nom n'existe pas
                    String ROW1 = ("INSERT INTO groupe(nomg) VALUES('" + ngp.getText().toString() + "');"); //l'ajout du groupe
                    db.execSQL(ROW1);
                    Cursor f = db.rawQuery("SELECT id FROM groupe where nomg='"+ ngp.getText().toString() + "';", null);
                    f.moveToFirst();
                    Integer id_g = f.getInt(0); //récupération de l'id du nouveau groupe
                    String ROW2 = ("INSERT INTO EstMembre(id_u, id_g, admin) VALUES('" + id_u + "','"+ id_g + "','1');"); //Le créateur devient membre et admin de son groupe
                    db.execSQL(ROW2);
                    mesgroupes.add(ngp.getText().toString());
                    arrayAdapter.notifyDataSetChanged();
                }
                else //Si le nom de groupe existe deja
                    Toast.makeText(getApplicationContext(), "Ce nom existe déja!", Toast.LENGTH_SHORT).show();
            }
        };
        add.setOnClickListener(addListener);
    }

    /* Méthode qui définit le comportement de l'application quand on se déconnecte */
    private OnClickListener deconnexionListener= new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent my1 = new Intent(accueil.this, MainActivity.class); //redirection vers l'activité Main
            startActivity(my1); //exécution
        }
    };

    /* Méthode qui définit le comportement de l'application quand on appuie sur le bouton qui nous permet de créer un nouveau groupe (qui affiche le champ de saisie du nom et le bouton + */
    private OnClickListener generateListener= new OnClickListener() {
        @Override
        public void onClick(View v) {
            add.setVisibility(View.VISIBLE);
            ngp.setVisibility(View.VISIBLE);
            generate.setVisibility(View.GONE);
        }
    };
}
