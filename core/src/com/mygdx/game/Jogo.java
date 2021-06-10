package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Random;

import javax.swing.text.html.ImageView;

public class Jogo extends ApplicationAdapter {

    private float larguraDispositivo; //Largura do Pano de Fundo
    private float alturaDispositivo; //Altura do Pano de Fundo
    private float posicaoInicialVerticalPassaro = 0; //Posição inicial vai se iniciar no zero
    private float variacao = 0; //Variação das imagens do passaro para gerar o movimento das asa
    private float posicaoCanoHorizontal; //Posição do cano no sentido horizontal
    private float posicaoCanoVertical; //Posição do cano no sentido Vertical
    private float posicaoHorizontalPassaro = 0; //A posição a qual o passaro vai estar no jogo
    private float espacoEntreCanos; //O espçao entre os canos de cima e o de baixo
    private int estadoJogo = 0; //Mudança de estados a qual o jogo irá sofrer
    private int pontos = 0; //Movimento do eixo Y
    private int pontuaçãoMaxima = 0; //A pontuação a qual o jogador vai receber pela sua classificação, ao dar Game Over irá mostrar o ponto máximo recebido
    private int gravidade = 0; //Gravidade do paasaro conforme se movimenta

    private Texture[] passaros; //As imagens dos sprites
    private Texture fundo; //A imagem de fundo
    private Texture canoBaixo; //A imagem do Cano Baixo
    private Texture canoTopo; //A imagem do cano de cima
    private Texture gameOver; //A imagem do Game Over
    private SpriteBatch batch; //Quantidades de sprites que vai ser criado
    private boolean passouCano = false; //Condição de que se passou no cano for verdadeiro ou não
    private Random random; //O aleatório do cano em relação a sua posição

    BitmapFont textoPontuacao; //A imagem da pontuação a qual vai ser intercalado
    BitmapFont textoReiniciar; //A imagem do texto reiniciar a ser intercalado
    BitmapFont textoMelhorPontuacao; //A imagem do texto da melhor pontuação a ser interclado

    Sound somVoando; //Som do passaro voando
    Sound somColisão; //Som da colisão
    Sound somPontuação; //Som da pontuação

    Preferences preferencias;

    private ShapeRenderer shapeRenderer; //Renderiza pontos, linhas, contornos e formas a ser preenchida
    private Circle circuloPassaro; //O raio do passaro
    private Rectangle retanguloCanoCima; //retangulo a qual o passaro ira bater no topo
    private Rectangle retanguloCanoBaixo; //retangulo a qual o passaro ira bater na perte de baixo


    @Override
    public void create () {
        inicializaTexturas();
        inicializaObjetos();


    }

    private void inicializaObjetos() {

        random = new  Random(); //Instanciando o Random dos objetos
        batch = new SpriteBatch(); //Instanciando um novo lotes de sprites

        larguraDispositivo = Gdx.graphics.getWidth(); //Pegar a largura do pano de fundo
        alturaDispositivo = Gdx.graphics.getHeight(); //Pegar a altura do pano de fundo
        posicaoInicialVerticalPassaro = alturaDispositivo / 2; //Fazer a posição inicial ficar entre o centro da tela e não mais na posição 0
        posicaoCanoHorizontal = larguraDispositivo; //
        espacoEntreCanos = 350; //O espaço que separa o cano de cima com o cano de baixo

        textoPontuacao = new BitmapFont(); //Instanciando a fonte do texto pontuação
        textoPontuacao.setColor(Color.WHITE); //A cor que vai ser destacado nos pontos
        textoPontuacao.getData().setScale(10); //Aqui voce pega os dados dos pontos e tambem define a escala da pontuação

        textoReiniciar = new BitmapFont(); //Instanciar a fonte do texto reiniciar
        textoReiniciar.setColor(Color.GREEN); //A cor que vai ser destacado no texto
        textoReiniciar.getData().setScale(3); //Aqui voce pega os dados dos pontos e tambem define a escala do texto

        textoMelhorPontuacao = new BitmapFont(); //Instanciar a fonte do texto melhor pontuação
        textoMelhorPontuacao.setColor(Color.RED); //A cor que vai ser destacado no texto
        textoMelhorPontuacao.getData().setScale(3); //Aqui voce pega os dados dos pontos e tambem define a escala do texto

        //As Instancias do shape, circulo do passaro, retangulo do cano de cima, retangulo do cano de baixo
        shapeRenderer = new ShapeRenderer();
        circuloPassaro = new Circle();
        retanguloCanoCima = new Rectangle();
        retanguloCanoBaixo = new Rectangle();

        somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav")); //implementação do som da asa
        somColisão = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav")); //implementação do som da batida
        somPontuação = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav")); //implementação do som dos pontos

        preferencias = Gdx.app.getPreferences("flappyBird");
        pontuaçãoMaxima = preferencias.getInteger("pontuacaoMaxima",0); //Ele pega as pontuações e ao reiniciar volta ao zero
    }

    private void inicializaTexturas() {

        passaros = new Texture[3]; //Instanciando a quantidade de sprites a ser colocado
        passaros[0] = new Texture("passaro1.png"); //Imagem do passaro 1
        passaros[1] = new Texture("passaro2.png"); //Imagem do passaro 2
        passaros[2] = new Texture("passaro3.png"); //Imagem do passaro 3

        fundo = new Texture("fundo.png"); //Imagem do pano de fundo
        canoBaixo = new Texture("cano_baixo_maior.png"); //Imagem do cano inferior
        canoTopo = new Texture("cano_topo_maior.png"); //Imagem do cano superior
        gameOver = new Texture("game_over.png"); //A imagem do Game Over
    }


    @Override
    public void render () {

        verificaEstadoJogo();
        desenharTexturas();
        detectarColisão();
        validarPontos();

    }

    //Aqui define as posições do passaro e dos canos baixo e cima,
    private void detectarColisão() {
        circuloPassaro.set(50 + passaros[0].getWidth() / 2, posicaoInicialVerticalPassaro + passaros[0].getHeight() / 2, passaros[0].getWidth() / 2);

        retanguloCanoBaixo.set(posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical, canoBaixo.getWidth(), canoBaixo.getHeight());

        retanguloCanoCima.set(posicaoCanoHorizontal, alturaDispositivo / 2 - canoTopo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical, canoTopo.getWidth(), canoTopo.getHeight());

        boolean bateuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima); //Verifica se os sprites convexos se sobrepõem
        boolean bateuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);

        //Ao bater no cano de cima ou no cano de baixo, sai o som
        if(bateuCanoBaixo || bateuCanoCima){
            if(estadoJogo == 1){
                somColisão.play();
                estadoJogo = 2;
            }
        }
    }

    //Ao passar pelo cano, ele vai pontuar e vai fazendo isso sucessivamnete
    private void validarPontos() {
        if(posicaoCanoHorizontal < 50 - passaros[0].getWidth()){
            if(!passouCano){
                pontos++;
                passouCano = true;
                somPontuação.play(); //O som da pontuação ao passar o cano
            }
        }
        variacao += Gdx.graphics.getDeltaTime() * 10; //Suaviza a animação do bater das asas do passaro ao multiplicar por 10
        if(variacao > 3) //Se aa imagens do sprites ultrapassar de três, faz alguma coisa
            variacao = 0; //A imagem for maior que 3 retorna para zero
    }

    //Os estados a sofrer mudanças conforme as ações
    private void verificaEstadoJogo() {

        //Faz a posição inicial ao sofrer gravidade, mas ao tocar na tela ele flutua
        boolean toqueTela = Gdx.input.justTouched(); //Ao tocar na tela faz algo

        if(estadoJogo == 0) {


            if (toqueTela) { //Se ao clicar no touch
                gravidade = -25; //Faz o passaro flutuar para cima a cada toque na tela
                estadoJogo = 1;
                somVoando.play();
            }

        } else if (estadoJogo == 1) {

            if (toqueTela) { //Se ao clicar no touch
                gravidade = -25; //Faz o passaro flutuar para cima a cada toque na tela
                somVoando.play(); //A cada toque que faz o passaro voar, sai um som
            }

            posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200; //Aqui fará com que o passaro ao colidir com o cano, ele não fique congelado na ultima posição
            if (posicaoCanoHorizontal < -canoBaixo.getHeight()) {
                posicaoCanoHorizontal = larguraDispositivo;
                posicaoCanoVertical = random.nextInt(400) - 200;
                passouCano = false;
            }

            if (posicaoInicialVerticalPassaro > 0 || toqueTela) //Se a posição inicial for maior que zero e tocar na tela
                posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;

            gravidade++; //Adiciona gravidade
        }else if(estadoJogo == 2){

            if (pontos > pontuaçãoMaxima){
                pontuaçãoMaxima = pontos;
                preferencias.putInteger("pontuacaoMaxima", pontuaçãoMaxima);
            }

            posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;

            //Ao tocar na tela, se inicia o jogo, mas por ora, eles ficarão congelados na tela
            if(toqueTela){
                estadoJogo = 0;
                pontos = 0;
                gravidade = 0;
                posicaoHorizontalPassaro = 0;
                posicaoInicialVerticalPassaro = alturaDispositivo / 2;
                posicaoCanoHorizontal = larguraDispositivo;
            }
        }
    }

    //O desenho dos sprites que forem implementados no jogo
    private void desenharTexturas() {

        batch.begin(); //Inicio do processo

        batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo); //Desenha o fundo da tela
        batch.draw(passaros[(int) variacao], 50 + posicaoHorizontalPassaro, posicaoInicialVerticalPassaro); //Desenha a posição, a altura e a largura do pássaro
        batch.draw(canoBaixo, posicaoCanoHorizontal, alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical);
        batch.draw(canoTopo, posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical);
        textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo /2, alturaDispositivo - 100);

        if(estadoJogo == 2){

            batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2, alturaDispositivo / 2);
            textoReiniciar.draw(batch, "TOQUE NA TELA PARA REINICIAR!", larguraDispositivo / 2 - 200, alturaDispositivo / 2 - gameOver.getHeight() / 2);
            textoMelhorPontuacao.draw(batch, "SUA MELHOR PONTUAÇÃO É: " + pontuaçãoMaxima + " PONTOS!", larguraDispositivo / 2 - 300, alturaDispositivo / 2 - gameOver.getHeight() * 2);
        }


        batch.end(); //Fim do processo
    }

    @Override
    public void dispose () {

    }

}
