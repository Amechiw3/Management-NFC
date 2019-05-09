package com.amechiw3.managementnfc;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.Preference;
import android.support.constraint.solver.widgets.*;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

// IMPORT EXTRAS
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.v7.graphics.drawable.DrawableWrapper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.tech.MifareClassic;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity {

    private final int DIALOG_SALDO = 1;
    private final int DIALOG_CARGAR = 2;
    private final int DIALOG_NUMERO = 3;
    private final byte[] KEY_A_SECTOR_1 = new byte[]{(byte)0x4D,(byte)0x53,(byte)0x58,(byte)0x4E,(byte)0x43,(byte)0x52};
    private final byte[] KEY_DEFAULT = new byte[]{(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF};

    private TapDialog tapDialog;

    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showTapDialog(getResources().getString(R.string.consultar_saldo), DIALOG_SALDO);
        inicializarNFC();
    }
    public void onCargarSaldo(View view) {
        showTapDialog(getResources().getString(R.string.cargar_saldo), DIALOG_CARGAR);
    }

    public void onConsultarSaldo(View view) {
        showTapDialog(getResources().getString(R.string.consultar_saldo), DIALOG_SALDO);
    }

    public void onCargarSaldoEdit(View view) {
        EditText etSaldo = (EditText)findViewById(R.id.txtSaldo);

        int saldox = Integer.parseInt(etSaldo.getText().toString());
        int totalsaldo = saldox * 100;

        Toast.makeText(this, ConvertirDecToHex(totalsaldo) + " ", Toast.LENGTH_LONG).show();
    }

    private void showTapDialog(String message, int tag) {
        if(tapDialog == null) {
            tapDialog = new TapDialog(this);
            tapDialog.setCanceledOnTouchOutside(false);
            tapDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        tapDialog.setTitle(message);
        tapDialog.setTag(tag);
        tapDialog.show();
    }

    private void inicializarNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "Este dispositivo no soporta NFC.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!mNfcAdapter.isEnabled()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("NFC deshabilitado");
            alertDialog.setMessage("Su NFC está deshabilitado.\nIngrese a las opciones de configuración y activelo.");
            alertDialog.show();
        }

        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("error", e);
        }

        mFilters = new IntentFilter[] {ndef};
        mTechList = new String[][] { new String[] { MifareClassic.class.getName() } };
    }

    @Override
    protected void onResume() {
        super.onResume();

        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechList);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    private String ConvertirDecToHex(int numero) {
        int Numero = numero * 100;
        String ResultadoHexa  = "";
        String Hex = "";
        String PreRes;

        while (Numero > 0)
        {
            PreRes = Numero % 16 +"";
            if (PreRes.equals("10"))
                Hex += "A";
            else if (PreRes.equals("11"))
                Hex += "B";
            else if (PreRes.equals("12"))
                Hex += "C";
            else if (PreRes.equals("13"))
                Hex += "D";
            else if (PreRes.equals("14"))
                Hex += "E";
            else if (PreRes.equals("15"))
                Hex += "F";
            else
                Hex += PreRes;
            Numero /= 16;
        }

        for (int i = Hex.length() - 1; i >= 0; i--)
        {
            ResultadoHexa += Hex.substring(i, 1);
        }

        return ResultadoHexa;
    }

    public  void saldohext(View view){
        EditText tvSaldoBIP = (EditText)findViewById(R.id.txtSaldo);
        int saldo = Integer.parseInt(tvSaldoBIP.getText().toString()) * 100;
        int total = 65535;
        int diferencia = total - saldo;

        String StrSaldo = Integer.toHexString(saldo).toString();
        String StrDiferencia = Integer.toHexString(diferencia).toString();

        byte a = Byte.decode("0x"+StrSaldo.substring(2));
        byte b = Byte.decode("0x"+StrSaldo.substring(0, 2));
        byte c = Byte.decode("0x"+StrSaldo.substring(0, 2));
        byte d = Byte.decode("0x"+StrDiferencia.substring(0,2));

        DATA_CARGA = new byte[]{(byte) a, (byte) b,(byte)0x00,(byte)0x00,
                (byte) c,(byte) d,(byte)0xff,(byte)0xff,
                (byte)0x50,(byte)0xC3,(byte)0x00,(byte)0x00,
                (byte)0x00,(byte)0x02,(byte)0x01,(byte)0x86};
        //tvSaldoBIP.setText("SALDO: " +  + StrSaldo.substring(2) + " DIFERENCIA: "+ StrDiferencia.substring(0,2) + StrDiferencia.substring(2));
        tvSaldoBIP.setText(DATA_CARGA.toString());
    }

    private byte[] DATA_CARGA;
    /*= new byte[]{(byte)0x50,(byte)0xC3,(byte)0x00,(byte)0x00,
                   (byte)0xAF,(byte)0x3C,(byte)0xff,(byte)0xff,
                   (byte)0x50,(byte)0xC3,(byte)0x00,(byte)0x00,
                   (byte)0x00,(byte)0x02,(byte)0x01,(byte)0x86};*/
    @Override
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);
            try {
                mfc.connect();
                TextView tvSaldoBIP = (TextView)findViewById(R.id.txtSaldo);

                if(tapDialog.isShowing()) {
                    switch (tapDialog.getTag()) {
                        case DIALOG_CARGAR:
                            if(mfc.authenticateSectorWithKeyA(0x01, KEY_A_SECTOR_1)) {
                                EditText txSaldoNew = (EditText) findViewById(R.id.txtSaldo);
                                if (Integer.parseInt(txSaldoNew.getText().toString()) > 0 && Integer.parseInt(txSaldoNew.getText().toString()) <= 500) {
                                    int saldo = Integer.parseInt(txSaldoNew.getText().toString()) * 100;
                                    int total = 65535;
                                    int diferencia = total - saldo;

                                    byte[] SaldoBytes = ByteBuffer.allocate(4).putInt(saldo).array();
                                    SaldoBytes[0] = (byte) 0x00;
                                    SaldoBytes[1] = (byte) 0x00;

                                    byte[] diferenceBytes = ByteBuffer.allocate(4).putInt(diferencia).array();
                                    diferenceBytes[0] = (byte) 0xFF;
                                    diferenceBytes[1] = (byte) 0xFF;

                                    DATA_CARGA = new byte[]{
                                            SaldoBytes[3], SaldoBytes[2], SaldoBytes[1], SaldoBytes[0],
                                            diferenceBytes[3], diferenceBytes[2], diferenceBytes[1], diferenceBytes[0],
                                            SaldoBytes[3], SaldoBytes[2], SaldoBytes[1], SaldoBytes[0],
                                            (byte) 0x00, (byte) 0x02, (byte) 0x01, (byte) 0x86
                                    };
                                    mfc.writeBlock(0x04, DATA_CARGA);
                                    Toast.makeText(this, "SALDO CARGADO", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(this, "Saldo ingresado excedido", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(this, "error autenticando sector 0x01", Toast.LENGTH_LONG).show();
                            }
                            break;
                        case DIALOG_NUMERO:
                            if(mfc.authenticateSectorWithKeyA(0x01, KEY_A_SECTOR_1)) {
                                byte[] data = mfc.readBlock(0x04);
                                Toast.makeText(this, "TU SALDO ES DE: "+ formatMoneda(leToNumeric(data, 2)), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "error autenticando sector 0x00", Toast.LENGTH_LONG).show();
                            }
                            break;
                    }
                }
                // ---- datos de la tarjeta --------------------------------------------------------
                //SALDO
                if(mfc.authenticateSectorWithKeyA(0x01, KEY_A_SECTOR_1)) {
                    byte[] data = mfc.readBlock(0x04);
                    tvSaldoBIP.setText(formatMoneda(leToNumeric(data, 2)));
                } else {
                    Toast.makeText(this, "No se Identifico Key A 0x01", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.e("Error ", e.getLocalizedMessage());
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            } finally {
                try {mfc.close();} catch (IOException e) {}
            }
        }
        tapDialog.dismiss();
    }

    private long leToNumeric(byte[] buffer, int size) {
        long value = 0;
        for (int i=0; i<size; i++) { value += ((long) buffer[i] & 0xffL) << (8 * i);}
        return value;
    }

    private String formatMoneda(long valor) {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols decimalFormatSymbols =  ((DecimalFormat) format).getDecimalFormatSymbols();
        decimalFormatSymbols.setCurrencySymbol("");
        ((DecimalFormat) format).setDecimalSeparatorAlwaysShown(true);
        ((DecimalFormat) format).setDecimalFormatSymbols(decimalFormatSymbols);

        valor = valor / 100;

        return format.format(valor);
        //return "$"+String.format("%d", valor);

    }
}
