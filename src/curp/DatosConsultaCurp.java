/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package curp;

/**
 *
 * @author User
 */
public class DatosConsultaCurp {
    private String usuario="wsgestion";
    private String password="wsgestion2011";
    private int tipoTransaccion=1;
    private String direccionIp="201.139.74.6";
    private String cveCurp="";
private String cveEntidadEmisora="20";

    public void setCveCurp(String cveCurp) {
        this.cveCurp = cveCurp;
    }

    public void setCveEntidadEmisora(String cveEntidadEmisora) {
        this.cveEntidadEmisora = cveEntidadEmisora;
    }

    public String getCveCurp() {
        return cveCurp;
    }

    public String getCveEntidadEmisora() {
        return cveEntidadEmisora;
    }
    
    
    public String getUsuario() {
        return usuario;
    }

    public String getPassword() {
        return password;
    }

    public int getTipoTransaccion() {
        return tipoTransaccion;
    }

    public String getDireccionIp() {
        return direccionIp;
    }
   

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setPassword(String passwod) {
        this.password = passwod;
    }

    public void setTipoTransaccion(int tipoTransaccion) {
        this.tipoTransaccion = tipoTransaccion;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }
    
    
   
    
}
