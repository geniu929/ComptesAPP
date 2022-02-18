package com.example.eugen.comptesapp;

import android.app.ActionBar;
import java.lang.Math;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewDebug;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.abs;

/**
 * Created by abdou on 08/06/17.
 */

public class group extends Activity {
    Button rejoindre = null;
    ListView list = null;
    TextView pseudo = null;
    TextView textView2 = null;
    Button quitter = null;
    Button calcul = null;
    Button ajouterfact = null;
    EditText motif = null;
    EditText montant = null;
    TextView dcnx = null;
    TextView result = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group);
        pseudo = (TextView) findViewById(R.id.pseudo);
        calcul = (Button)findViewById(R.id.calcul);
        dcnx = (TextView) findViewById(R.id.dcnx);
        dcnx.setOnClickListener(deconnexionListener);
        ajouterfact = (Button)findViewById(R.id.ajouterfact);
        motif = (EditText)findViewById(R.id.motif);
        montant = (EditText)findViewById(R.id.montant);
        quitter = (Button)findViewById(R.id.quitter);
        View.OnClickListener ajouterListener = null;
        View.OnClickListener quitterListener = null;
        result = (TextView)findViewById(R.id.result);
        View.OnClickListener calculListener = null;
        rejoindre = (Button) findViewById(R.id.rejoindre);
        list = (ListView) findViewById(R.id.list);
        textView2 = (TextView)findViewById(R.id.textView2);

        /* Masquer les bouton qui doivent apparaitre seulement si on rejoint le groupe*/
        calcul.setVisibility(View.GONE);
        ajouterfact.setVisibility(View.GONE);
        montant.setVisibility(View.GONE);
        motif.setVisibility(View.GONE);
        quitter.setVisibility(View.GONE);

        /* Récupération des données récus de l'activité precedente */
        final String nom_u = getIntent().getStringExtra("nom_u");
        final String id_u = getIntent().getStringExtra("id_u");
        final String nom_g = getIntent().getStringExtra("nom_g");

        pseudo.setText("Bonjour, " + nom_u +"  "); //Modification de l'entéte

        textView2.setText(" Rejoignez ce groupe pour voir ses détails ");

        final SQLiteDatabase db = openOrCreateDatabase("projet", Context.MODE_PRIVATE, null); //démarrage de la base

        db.execSQL("CREATE TABLE IF NOT EXISTS Factures( id_f INTEGER PRIMARY KEY AUTOINCREMENT, id_g INTEGER, id_u INTEGER, montant REAL, motif TEXT);"); //Création de la table facture

        /* Création de la liste qui contient les factures et son Adaptateur */
        final ListView lv = (ListView) findViewById(R.id.list);
        final List<String> mesusers = new ArrayList<String>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, mesusers);
        lv.setAdapter(arrayAdapter);

        final Cursor d = db.rawQuery("SELECT * from groupe WHERE nomg='" + nom_g + "'", null);
        d.moveToFirst();
        final String id_g = d.getString(0); //récupération de l'id du groupe

        /* Affichage des factures sur une liste */
        Cursor c =db.rawQuery("SELECT distinct u.pseudo, f.montant, f.motif FROM Factures f inner join utilisateurs u on u.id = f.id_u WHERE f.id_g = '" + id_g + "'", null);
        if (c.getCount()==0){ // Table facture vide
            mesusers.add("Aucune facture présente");
            arrayAdapter.notifyDataSetChanged();
        }
        while(c.moveToNext()) {
            if(c.getFloat(1)>0.0){
                mesusers.add(c.getString(0) + " a dépensé "+c.getString(1)+"€: "+c.getString(2));
                arrayAdapter.notifyDataSetChanged();
            }
        }

        result.append("les membres de ce groupe sont: ");

        /* Affichage des membres du groupe */
        final Cursor de = db.rawQuery("SELECT u.pseudo from EstMembre m inner join utilisateurs u on u.id = m.id_u WHERE m.id_g='" + id_g + "'", null);
        de.moveToFirst();
        while(!de.isAfterLast()){
            result.append(de.getString(0)+ " ; ");
            de.moveToNext();
        }

        /* Affichage des boutons qui sont résérvés pour les membres du groupe */
        Cursor x = db.rawQuery("SELECT id_u FROM EstMembre where id_g = '" + id_g + "'", null);
        final int i = Integer.parseInt(id_u);
        x.moveToFirst();
        while (x.moveToNext()) {
            if (i == x.getInt(0)) {
                rejoindre.setVisibility(View.GONE);
                calcul.setVisibility(View.VISIBLE);
                quitter.setVisibility(View.VISIBLE);
                ajouterfact.setVisibility(View.VISIBLE);
                motif.setVisibility(View.VISIBLE);
                montant.setVisibility(View.VISIBLE);
                textView2.setText("Vous étes dans le groupe: "+ nom_g + ", Voici les factures présentes: ");
            }

        }

        /* Si l'utilisateur est l'administrateur du groupe, le bouton quitter devient supprimer. */
        Cursor y =db.rawQuery("SELECT admin FROM EstMembre WHERE id_g = '" + id_g + "' AND id_u ='"+id_u+"';", null);
        y.moveToFirst();
        int admin2 =0;
        if(y.getCount()>0){
            final int admin = y.getInt(0);
            if (admin==1){
                quitter.setText("Supprimer le groupe");
                textView2.setText("Vous étes l'admin du groupe: "+ nom_g + ", Voici les factures présentes: ");
                admin2=1;
            }
        }

        /* Méthode qui définit le comportement de l'application quand on essaie d'avoir une solution de paiement égal
         * Cette méthode est tellement compliquée que je n'arrive pas à l'expliquer en commentaires
         * Je préfére donc le faire devant vous le jour de la démonstration */
        calculListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Cursor dd = db.rawQuery("SELECT SUM(montant) FROM Factures where id_g='" + id_g + "';", null);
                final Cursor e = db.rawQuery("SELECT u.pseudo, SUM(f.montant), f.id_g FROM Factures f inner join utilisateurs u on f.id_u = u.id where id_g='"+id_g+"' GROUP BY u.pseudo;", null);
                e.moveToFirst();
                result.append(String.valueOf(e.getCount()));
                while(!e.isAfterLast()){
                    result.append(e.getString(0)+ "    ");
                    result.append(e.getFloat(1)+ "    ");
                    result.append("\n");
                    e.moveToNext();
                }
                final String [][] calculs = new String [e.getCount()+1][2+(e.getCount())];
                dd.moveToFirst();
                Float s = dd.getFloat(0);
                result.setText("Vous avez un total de "+ s +"€");
                result.append("\n");
                Float p = s/e.getCount(); // part de chacun
                result.append("chacun doit dépenser " + p + "€\n");
                result.append("Solution proposée: \n");
                e.moveToFirst();
                for(int u=1;u<e.getCount()+1;u++){
                    for(int m=0;m<2;m++){
                        if (m==0){
                            calculs[u][m]=e.getString(0);
                        }
                        else{
                            if(p-e.getFloat(1)>0)
                                calculs[u][m]= String.valueOf(p-e.getFloat(1));
                            else
                                calculs[u][m]= String.valueOf(p-e.getFloat(1));
                        }
                    }
                    e.moveToNext();
                }
                e.moveToFirst();
                for(int m=2;m<(e.getCount()+2);m++){
                    calculs[0][m]=e.getString(0);
                    e.moveToNext();

                }

                while(verif(calculs, e.getCount())!=0){
                    Float max = Float.valueOf(0);
                    Float ValMax = Float.valueOf(0);
                    String NomMax=null;
                    for(int u=1;u<e.getCount()+1;u++){
                        if(Float.valueOf(calculs[u][1])>max){
                            max = Float.valueOf(calculs[u][1]);
                            NomMax = calculs[u][0];
                            ValMax = Float.valueOf(calculs[u][1]);
                        }
                    }
                    e.moveToFirst();
                    Float min = Float.valueOf(999);
                    Float ValMin = Float.valueOf(0);
                    Float donne = Float.valueOf(0);
                    String NomMin=null;
                    for(int u=1;u<e.getCount()+1;u++){
                        if(Float.valueOf(calculs[u][1])<min){
                            min = Float.valueOf(calculs[u][1]);
                            NomMin = calculs[u][0];
                            ValMin = Float.valueOf(calculs[u][1]);
                        }
                    }
                    if(ValMax-abs(ValMin)>=0){
                        for(int u=1;u<e.getCount()+1;u++){
                            if(calculs[u][0].equals(NomMin)){
                                calculs[u][1]=String.valueOf(0);;
                                donne = abs(ValMin);
                            }
                        }
                        for(int u=1;u<e.getCount()+1;u++){
                            if(calculs[u][0].equals(NomMax)){
                                calculs[u][1]=String.valueOf(ValMax-abs(ValMin));
                            }
                        }
                    }
                    else {
                        for(int u=1;u<e.getCount()+1;u++){
                            if(calculs[u][0].equals(NomMin)){
                                calculs[u][1]=String.valueOf(ValMax-abs(ValMin));
                                donne = abs(ValMax);
                            }
                        }
                        for(int u=1;u<e.getCount()+1;u++){
                            if(calculs[u][0]==NomMax){
                                calculs[u][1]=String.valueOf(0);
                            }
                        }
                    }
                    result.append(NomMax + " donne à " + NomMin + " " + donne +"€\n");
                }
                for(int u=0;u<e.getCount()+1;u++){
                    for(int m=0;m<2+(e.getCount());m++){
                        calculs[u][m]=null;
                    }
                }
            }
        };
        calcul.setOnClickListener(calculListener);

        final int finalAdmin = admin2;

        /* Méthode qui définit le comportement de l'application quand on essaie de quitter un groupe */
        quitterListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (finalAdmin == 1){ // Si l'utilsateur est admin (il s'agit donc de supprimer le groupe
                    String ROW1 = ("DELETE FROM groupe where id = '" + id_g + "';"); // Suppression tous les détails du groupe
                    db.execSQL(ROW1);
                    String ROW2 = ("DELETE FROM EstMembre where id_g = '" + id_g + "';"); // Suppression de tous les membres du groupe
                    db.execSQL(ROW2);
                    String ROW3 = ("DELETE FROM Factures where id_g = '" + id_g + "';"); // Suppression de toutes les factures
                    db.execSQL(ROW3);
                    Intent my1 = new Intent(group.this, accueil.class); //Redirection vers l'activité d'accueil
                    my1.putExtra("nom", nom_u); //On envoie le nom de l'utilsateur
                    startActivity(my1); //exécution

                }
                else{ // Sin l'utilsateur n'est pas l'admin (il s'agit de quitter le groupe)
                    String ROW1 = ("DELETE FROM Factures where id_u = '" + Integer.parseInt(id_u) + "';"); //suppression des factures de l'utilisateur
                    String ROW2 = ("DELETE FROM EstMembre where id_u = '" + Integer.parseInt(id_u) + "' AND id_g = '"+Integer.parseInt(id_g)+"';"); // Suppression de la ligne qui définit son existence dans le groupe
                    db.execSQL(ROW1);
                    db.execSQL(ROW2);
                    finish();
                    startActivity(getIntent());
                }

            }
        };
        quitter.setOnClickListener(quitterListener);

        /* Méthode qui définit le comportement de l'application quand on essaie d'ajouter une facture au groupe */
        ajouterListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (motif.getText().toString().equals("") || montant.getText().toString().equals("")) { //si le motif ou le montant est vide
                    Toast.makeText(getApplicationContext(), "Indiquez le montant et le motif de la facture", Toast.LENGTH_LONG).show();
                } else { //Sinon on ajoute la facture
                    String ROW1 = ("INSERT INTO Factures(id_u, id_g, montant, motif) VALUES('" + Integer.parseInt(id_u) + "','" + Integer.parseInt(id_g) + "','" + Float.parseFloat(montant.getText().toString()) + "','" + motif.getText().toString() + "');");
                    db.execSQL(ROW1);
                    list.invalidate(); //refraichir la liste
                }
            }};
        ajouterfact.setOnClickListener(ajouterListener);

        /* Méthode qui définit le comportement de l'application quand on essaie de rejoindre le groupe */
        View.OnClickListener rejoindreListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = Integer.parseInt(id_u); //récupération de l'id de l'utilsateur
                final Cursor d = db.rawQuery("SELECT * from EstMembre WHERE id_u='" + i + "' AND id_g= '" + Integer.parseInt(id_g) + "'", null);
                if (d.getCount() != 0) { //vérifier si l'utilsateur est déja membre
                    Toast.makeText(getApplicationContext(), "Vous faites deja partie de ce groupe ", Toast.LENGTH_LONG).show();
                    /* Mise à jour des visibiltés des boutons */
                    rejoindre.setVisibility(View.GONE);
                    calcul.setVisibility(View.VISIBLE);
                    quitter.setVisibility(View.VISIBLE);
                    ajouterfact.setVisibility(View.VISIBLE);
                    motif.setVisibility(View.VISIBLE);
                    montant.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.VISIBLE);
                    textView2.setText("Vous étes dans le groupe: " + nom_g + ", Voici les factures présentes: ");
                }
                else { //Si l'utilsateur n'est pas membre
                    String ROW1 = ("INSERT INTO EstMembre VALUES('" + id_u + "','" + id_g + "','0');"); //Mise à jour de la table
                    db.execSQL(ROW1);
                    String ch = "";
                    String ROW3 = ("INSERT INTO Factures(id_u, id_g, montant, motif) VALUES('" + Integer.parseInt(id_u) + "','" + Integer.parseInt(id_g) + "','" + 0 + "','" + ch + "');"); //On rajoute quand meme une facture qui vaut  à son nom, cette insertion nous est indispensable dans la méthode du calcul meme si la facture est de .
                    db.execSQL(ROW3);
                    /* Mise à jour des visibiltés des boutons */
                    rejoindre.setVisibility(View.GONE);
                    calcul.setVisibility(View.VISIBLE);
                    quitter.setVisibility(View.VISIBLE);
                    ajouterfact.setVisibility(View.VISIBLE);
                    motif.setVisibility(View.VISIBLE);
                    montant.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.VISIBLE);
                    textView2.setText("Vous étes dans le groupe: " + nom_g + ", Voici les factures présentes: ");
                }
            }
        };
        rejoindre.setOnClickListener(rejoindreListener);
    };

    /* Méthode qui définit le comportement de l'application quand on se déconnecte */
    private View.OnClickListener deconnexionListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent my1 = new Intent(group.this, MainActivity.class); //redirection vers l'activité Main
            startActivity(my1); // exécution
        }
    };

    /* Cette méthode vérifie si la deuxiéme colonne d'un tableau bidimensionnelle ne contient que des zeros
     * Elle nous est utile lors du calcul de la solution */
    public Integer verif(String[][] a, int t) {
        Integer res = 0;
        for (int u = 1; u < t + 1; u++) {
            if (Float.valueOf(a[u][1]) != 0) {
                res++;
            }

        }
        return res;
    }
}
