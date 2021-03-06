/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufscar.dc.hotel.dao;

import br.ufscar.dc.hotel.beans.Hotel;
import br.ufscar.dc.hotel.beans.Promocao;
import br.ufscar.dc.hotel.beans.Site;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author victo
 */
@RequestScoped
public class PromocaoDAO {

    private final static String CRIAR_PROMOCAO_SQL = "insert into Promocoes"
            + " (url_site, cnpj_hotel, preco, data_inicial, data_final)"
            + " values (?,?,?,?,?)";

    private final static String LISTAR_PROMOCAO_HOTEL_SQL = "select"
            + " p.preco, p.data_inicial, p.data_final, s.nome, s.telefone"
            + " from Promocoes as p, Site as s"
            + " where (p.cnpj_hotel=?) and (p.url_site = s.url)";

    private final static String LISTAR_PROMOCAO_SITE_SQL = "select"
            + " p.preco, p.data_inicial, p.data_final, h.nome, h.cidade"
            + " from Promocoes as p, Hotel as h"
            + " where (p.url_site=?) and (p.cnpj_hotel = h.cnpj)";

    private final static String VERIFICA_PROMOCAO_HOTEL_SQL = "select"
            + " p.data_inicial, p.data_final"
            + " from Promocoes as p, Hotel as h"
            + " where (p.cnpj_hotel=?) and (p.cnpj_hotel = h.cnpj) and (p.data_inicial=?) and (p.data_final=?)";

    private final static String VERIFICA_PROMOCAO_SITE_SQL = "select"
            + " p.data_inicial, p.data_final"
            + " from Promocoes as p, Site as s"
            + " where (p.url_site=?) and (p.url_site = s.url) and (p.data_inicial=?) and (p.data_final=?)";

    @Resource(name = "jdbc/HotelDBLocal")
    DataSource dataSource;

    public Promocao gravarPromocao(Promocao p) throws SQLException, NamingException {
        try (Connection con = dataSource.getConnection();
                PreparedStatement ps = con.prepareStatement(CRIAR_PROMOCAO_SQL);) {
            ps.setString(1, p.getUrl().getUrl());
            ps.setString(2, p.getCnpj().getCnpj());
            ps.setDouble(3, p.getPreco());
            //Formato data
            SimpleDateFormat out = new SimpleDateFormat("yyyy-mm-dd");
            //Instanciando as duas datas obtidas
            java.sql.Date dI = new java.sql.Date(p.getData_inicial().getTime());
            java.sql.Date dF = new java.sql.Date(p.getData_final().getTime());
            //incrementando 1 dia, pois estava adicionando com 1 dia a menos no banco, sem motivo aparente
            dI.setDate(dI.getDate()+1);
            dF.setDate(dF.getDate()+1);
            //Convertendo para string no formato correto
            String sDi = out.format(dI);
            String sDf = out.format(dF);
            //valueOf recebe a data no formato String como argumento e retorna um objeto Date
            ps.setDate(4, java.sql.Date.valueOf(sDi));
            ps.setDate(5, java.sql.Date.valueOf(sDf));
            ps.execute();
        }
        return p;
    }
    

    public List<Promocao> listarPromocaoHotel(String cnpjHotel) throws SQLException {
        List<Promocao> ret = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
                PreparedStatement ps = con.prepareStatement(LISTAR_PROMOCAO_HOTEL_SQL)) {

            ps.setString(1, cnpjHotel);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Promocao p = new Promocao();
                    Site s = new Site();

                    s.setNome(rs.getString("nome"));
                    s.setTelefone(rs.getString("telefone"));

                    p.setPreco(rs.getDouble("preco"));
                    p.setData_inicial(rs.getDate("data_inicial"));
                    p.setData_final(rs.getDate("data_final"));
                    p.setUrl(s);

                    ret.add(p);
                }
            }
        }
        return ret;
    }

    public List<Promocao> listarPromocaoSite(String urlSite) throws SQLException {
        List<Promocao> ret = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
                PreparedStatement ps = con.prepareStatement(LISTAR_PROMOCAO_SITE_SQL)) {

            ps.setString(1, urlSite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Promocao p = new Promocao();
                    Hotel h = new Hotel();

                    h.setNome(rs.getString("nome"));
                    h.setCidade(rs.getString("cidade"));

                    p.setPreco(rs.getDouble("preco"));
                    p.setData_inicial(rs.getDate("data_inicial"));
                    p.setData_final(rs.getDate("data_final"));
                    p.setCnpj(h);

                    ret.add(p);
                }
            }
        }
        return ret;
    }

    public boolean verificarPromocaoHotel(String cnpjHotel, Date data_inicial, Date data_final) throws SQLException {
        boolean existe = false;
        try (Connection con = dataSource.getConnection();
                PreparedStatement ps = con.prepareStatement(VERIFICA_PROMOCAO_HOTEL_SQL)) {
            
            
            SimpleDateFormat out = new SimpleDateFormat("yyyy-mm-dd");
            //Instanciando as duas datas obtidas
            java.sql.Date dI = new java.sql.Date(data_inicial.getTime());
            java.sql.Date dF = new java.sql.Date(data_final.getTime());
            //incrementando 1 dia, pois estava adicionando com 1 dia a menos no banco, sem motivo aparente
            dI.setDate(dI.getDate()+1);
            dF.setDate(dF.getDate()+1);
            //Convertendo para string no formato correto
            String sDi = out.format(dI);
            String sDf = out.format(dF);

            ps.setString(1, cnpjHotel);
            ps.setDate(2, java.sql.Date.valueOf(sDi));
            ps.setDate(3, java.sql.Date.valueOf(sDf));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("true");
                    existe = true;
                }
            }
        }
        return existe;
    }

    public boolean verificarPromocaoSite(String urlSite, Date data_inicial, Date data_final) throws SQLException {
        boolean existe = false;
        try (Connection con = dataSource.getConnection();
                PreparedStatement ps = con.prepareStatement(VERIFICA_PROMOCAO_SITE_SQL)) {

            ps.setString(1, urlSite);
            ps.setDate(2, (java.sql.Date) data_inicial);
            ps.setDate(3, (java.sql.Date) data_final);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existe = true;
                }
            }
        }
        return existe;
    }
}
