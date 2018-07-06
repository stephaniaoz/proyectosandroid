package cohesivo.boardactivity.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import cohesivo.boardactivity.R;
import cohesivo.boardactivity.adapters.BoardAdapter;
import cohesivo.boardactivity.models.Board;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class BoardActivity extends AppCompatActivity implements
        RealmChangeListener<RealmResults<Board>>, AdapterView.OnItemClickListener {

    private Realm realm;

    private FloatingActionButton fab;
    private ListView listView;
    private BoardAdapter adapter;

    private RealmResults<Board> boards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        // Db Realm:
        realm = Realm.getDefaultInstance();
        boards = realm.where(Board.class).findAll();
        //se puede hacer también, en vez de hacer implemnets RealmChangeListener hacerlo donde
        // actualmente tenemos this con el método onChange, en este caso como usamos el
        // implements entonces se sobre escribe el método y el this lo que hace es buscar el
        // onChange en esta la clase
        boards.addChangeListener(this);

        adapter = new BoardAdapter(this, boards, R.layout.list_view_board_item);
        listView = (ListView) findViewById(R.id.listViewBoard);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        fab = (FloatingActionButton) findViewById(R.id.fabAddBoard);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertForCreatingBoard("Add a new board","Type a name for your new board");
            }
        });

        registerForContextMenu(listView);

        /*
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();*/

    }

    //** CRUD Actions **/
    private void createNewBoard(final String boardName) {


        //2 formas de hacer transacciones:
        /*
        realm.beginTransaction();
        Board board = new Board(boardName);
        //Antes daba error con copyFromRealm
        realm.copyToRealm(board);
        realm.commitTransaction();
        */

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Board board = new Board(boardName);
                //Lo que hace ese método es copiar a tu base de datos tu objeto que heredaste de RealmObject,
                // en el caso de este vídeo es la clase Board la que hereda de RealmObject y es la que se añade a la base de datos de Realm.
                //Si la clase Board no hereda de RealmObject no se puede utilizar para agregarlo al Realm.
                realm.copyToRealm(board);
            }
        });

    }

    private void editBoard(String newName, Board board){
        realm.beginTransaction();
        board.setTitle(newName);
        realm.copyToRealmOrUpdate(board);
        realm.commitTransaction();
    }

    private void deleteBoard(Board board){
        realm.beginTransaction();
        board.deleteFromRealm();
        realm.commitTransaction();
    }

    private void deleteAll(){
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }

    //** Dialogs **/
    private void showAlertForCreatingBoard(String title, String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if(title != null) builder.setTitle(title);
        if(message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_board, null);
        //Le asignamos la vista a ese dialogo:
        builder.setView(viewInflated);
        //No se puede hacer directamente findviewbyid porque no es el layout principal del
        // onCreate (activity_board) si no que es el layout del viewInflated.
        final EditText input = (EditText) viewInflated.findViewById(R.id.editTextNewBoard);

        //Configurar acción del botón:
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Se recoge el nombre del editext, para esto, como es como otra clase, para
                // acceder al input debe ser de tipo final (indica que a esa variable solo se le puede asignar un valor u objeto una única vez) el Editext:
                String boardName = input.getText().toString().trim();
                if(boardName.length() > 0){
                    createNewBoard(boardName);
                }else{
                    Toast.makeText(getApplicationContext(), "The name is required to create a new" +
                            " Board", Toast.LENGTH_LONG).show();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAlertForEditingBoard(String title, String message, final Board board){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if(title != null) builder.setTitle(title);
        if(message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_board, null);
        //Le asignamos la vista a ese dialogo:
        builder.setView(viewInflated);
        //No se puede hacer directamente findviewbyid porque no es el layout principal del
        // onCreate (activity_board) si no que es el layout del viewInflated.
        final EditText input = (EditText) viewInflated.findViewById(R.id.editTextNewBoard);
        input.setText(board.getTitle());

        //Configurar acción del botón:
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Se recoge el nombre del editext, para esto, como es como otra clase, para
                // acceder al input debe ser de tipo final (indica que a esa variable solo se le puede asignar un valor u objeto una única vez) el Editext:
                String boardName = input.getText().toString().trim();
                if (boardName.length() == 0){
                    Toast.makeText(getApplicationContext(), "The name is requiered to edit the " +
                            "current board", Toast.LENGTH_LONG).show();
                }else if(boardName.equals(board.getTitle())){
                    Toast.makeText(getApplicationContext(), "The name is the same than it was " +
                            "before", Toast.LENGTH_LONG).show();
                } else {
                    editBoard(boardName, board);
                }

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /* Events */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_board_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.delete_all:
                deleteAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(boards.get(info.position).getTitle());
        getMenuInflater().inflate(R.menu.context_menu_board_activity, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();

        switch (item.getItemId()){
            case R.id.delete_board:
                deleteBoard(boards.get(info.position));
                return true;
            case R.id.edit_board:
                showAlertForEditingBoard("Edit Board", "Change the name of the board", boards.get
                        (info.position));
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    @Override
    public void onChange(RealmResults<Board> element) {
        //Refresca el adaptador. Objeto del listener
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(BoardActivity.this, NoteActivity.class);
        intent.putExtra("id",boards.get(position).getId());
        startActivity(intent);
    }
}
