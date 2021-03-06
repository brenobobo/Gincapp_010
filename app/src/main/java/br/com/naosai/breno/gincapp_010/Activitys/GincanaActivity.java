package br.com.naosai.breno.gincapp_010.Activitys;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.UUID;

import br.com.naosai.breno.gincapp_010.Adapter.EquipeAdapter;
import br.com.naosai.breno.gincapp_010.Adapter.GincanaAdapter;
import br.com.naosai.breno.gincapp_010.Control.ConfiguracaoFirebase;
import br.com.naosai.breno.gincapp_010.Control.ControlEquipe;
import br.com.naosai.breno.gincapp_010.Entidades.Equipe;
import br.com.naosai.breno.gincapp_010.Entidades.Gincana;
import br.com.naosai.breno.gincapp_010.R;

public class GincanaActivity extends AppCompatActivity {

    private TextView textViewNomeDaGincana;

    private String nomeDaGincana;
    private Button botaoCadastrarTime;

    private ListView listaEquipes;
    private ArrayAdapter adapter;
    private ArrayList<Equipe> equipes;
    private Equipe equipe;


    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListenerEquipe;

    @Override
    protected void onStart() {
        super.onStart();
        databaseReference.addValueEventListener(valueEventListenerEquipe);
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReference.removeEventListener(valueEventListenerEquipe);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gincana);


        final Bundle extra = getIntent().getExtras();

        if (extra != null) {
            nomeDaGincana = extra.getString("chave");

        }



        textViewNomeDaGincana = findViewById(R.id.textViewNomeDaGincanaId);

        textViewNomeDaGincana.setText(nomeDaGincana);

        botaoCadastrarTime = findViewById(R.id.bt_cadastrarTime);

        listaEquipes = findViewById(R.id.listaTimesId);

        botaoCadastrarTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(GincanaActivity.this);
                alertDialog.setTitle("Qual é o nome do time?");

                alertDialog.setCancelable(true);

                final EditText textoNome = new EditText(GincanaActivity.this);


                alertDialog.setView(textoNome);

                alertDialog.setPositiveButton("Criar time", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Equipe equipe = new Equipe();
                        ControlEquipe controlEquipe = new ControlEquipe();

                        String id = extra.getString("id");

                        equipe.setId(UUID.randomUUID().toString());
                        equipe.setIdGincana(id);
                        equipe.setNome(textoNome.getText().toString());
                        controlEquipe.salvarEquipe(equipe);

                    }
                });

                alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which){

                    }
                });

                alertDialog.create();
                alertDialog.show();
            }
        });

        equipes = new ArrayList<>();


        listaEquipes = findViewById(R.id.listaTimesId);
        adapter = new EquipeAdapter(GincanaActivity.this, equipes);


        listaEquipes.setAdapter(adapter);

        final String idGincana = extra.getString("id");
        databaseReference = ConfiguracaoFirebase.getFirebase().child("Equipe").child(idGincana);

        //Listener para recuperar gincanas
        valueEventListenerEquipe = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Limpar lista

                equipes.clear();

                //Listando gincanas
                for (DataSnapshot dados: dataSnapshot.getChildren()){

                    Equipe equipe = dados.getValue(Equipe.class);
                    equipes.add(equipe);
                }

                // atualizacao da mudança
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        };

        listaEquipes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                final Equipe e = equipes.get(position);
                android.app.AlertDialog.Builder alertDialogPonto = new android.app.AlertDialog.Builder(GincanaActivity.this);
                alertDialogPonto.setTitle("Quantos pontos esse time tem?");
                final EditText editPontos = new EditText(GincanaActivity.this);
                if(e.getPontos()== null){
                    editPontos.setText("0");
                }else{
                    editPontos.setText(e.getPontos());
                }

                alertDialogPonto.setView(editPontos);



                alertDialogPonto.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ControlEquipe ce = new ControlEquipe();
                        e.setPontos(editPontos.getText().toString());
                        e.setIdGincana(idGincana);

                        ce.atualizarEquipe(e.getId(),e);




                    }
                });

                alertDialogPonto.create();
                alertDialogPonto.show();

            }
        });

        listaEquipes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                android.app.AlertDialog.Builder alertDialogExcluir = new android.app.AlertDialog.Builder(GincanaActivity.this);

                alertDialogExcluir.setTitle("Deseja excluir essa equipe?");

                alertDialogExcluir.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ControlEquipe controlEquipe = new ControlEquipe();
                        equipe = equipes.get(position);
                        controlEquipe.excluirEquipe(equipe.getIdGincana(), equipe.getId());

                    }
                });

                alertDialogExcluir.create();
                alertDialogExcluir.show();

                return true;
            }
        });

    }
}
