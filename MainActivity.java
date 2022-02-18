package com.example.eugen.comptesapp;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;



public class MainActivity extends Activity {
    Button cnx = null;
    Button inscr = null;
    EditText pseudo = null;
    EditText pwd = null;
    TextView res = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //récuperation du layout de la page depuis le fichier XML
        /* Récupération des boutons de l'activité depuis le fichier XML correspondant */
        cnx = (Button) findViewById(R.id.cnx);
        inscr = (Button) findViewById(R.id.create);
        res = (TextView) findViewById(R.id.res) ;
        pseudo = (EditText) findViewById(R.id.pseudo);
        pwd = (EditText) findViewById(R.id.pwd);
        cnx.setOnClickListener(connexionListener);
        inscr.setOnClickListener(inscriptionListener);
        SQLiteDatabase db = openOrCreateDatabase("projet", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS utilisateurs(id INTEGER PRIMARY KEY AUTOINCREMENT ,pseudo TEXT, mdp TEXT );"); //Création de la table utilisateurs
    }

    /* Méthode qui définit le comportement de l'application quand on essaie de s'inscrire */

    public OnClickListener inscriptionListener= new OnClickListener() {
        @Override
        public void onClick(View v) {
            SQLiteDatabase db = openOrCreateDatabase("projet", Context.MODE_PRIVATE, null);
            Cursor d = db.rawQuery("SELECT pseudo from utilisateurs WHERE pseudo='" + pseudo.getText().toString() + "'", null);
            if (pseudo.getText().toString().equals("") || pwd.getText().toString().equals("")){ // si l'un des champs est vide
                Toast.makeText(getApplicationContext(), "Votre Pseudo/mot de passe ne peut pas etre vide ", Toast.LENGTH_LONG).show();
            }
            else if (d.getCount() != 0) { // si il existe deja un utilisateur avec le meme pseudo
                Toast.makeText(getApplicationContext(), "Pseudo existant ", Toast.LENGTH_LONG).show();
            }
            else {
                String ROW1 = ("INSERT INTO utilisateurs(pseudo, mdp) VALUES('"+ pseudo.getText().toString() +"','"+pwd.getText().toString()+"');"); //on ajoute l'utilisateur
                db.execSQL(ROW1);
                Intent my2 = new Intent(MainActivity.this, accueil.class); //redirection vers la page d'accueil
                my2.putExtra("nom", pseudo.getText().toString()); //Envoyer le nom de l'utilisateur à l'activité d'accueil
                startActivity(my2); //exécution
            }
        }
    };

    /* Méthode qui définit le comportement de l'application quand on essaie de se connecter */

    private OnClickListener connexionListener= new OnClickListener() {
        @Override
        public void onClick(View v) {
            SQLiteDatabase db = openOrCreateDatabase("projet", Context.MODE_PRIVATE, null);
            Cursor c =db.rawQuery("SELECT pseudo, mdp FROM utilisateurs WHERE pseudo='"+pseudo.getText().toString()+"'", null);
            c.moveToFirst();
            if (pseudo.getText().toString().equals("") || pwd.getText().toString().equals("")){ //si l'un des champs est vide
                Toast.makeText(getApplicationContext(), "Votre Pseudo/mot de passe ne peut pas etre vide ", Toast.LENGTH_LONG).show();
            }
            else if (c.getCount()==0){ //si le pseudo n'existe pas dans la liste des pseudos inscrits
                Toast.makeText(getApplicationContext(), "Pseudo introuvable! Réessayez ", Toast.LENGTH_LONG).show();
            }

            else {
                int d = 0;
                c.moveToFirst();
                while(!c.isAfterLast()) //parcour de l'output de la commande SQLite
                {
                    if (c.getString(1).equals(pwd.getText().toString()) ){ //Si le mot de passe est bon
                        d++;
                        Intent my = new Intent (MainActivity.this, accueil.class); //redirection vers la page d'accueil
                        my.putExtra("nom", pseudo.getText().toString()); //Envoyer le nom de l'utilisateur à l'activité d'accueil
                        startActivity(my); //exécution
                    }
                    c.moveToNext();

                }
                if (d==0)
                    Toast.makeText(getApplicationContext(), "Mot de passe faux ", Toast.LENGTH_LONG).show();
            }
        }
    };
}