package example.helloakka.actor.sample;

import akka.actor.typed.ActorSystem;

public class Main {

    public static void main(String[] args) {
        // ActorSystem 생성
        final ActorSystem<HelloWorldActor.Greet> helloWorldSystem = ActorSystem.create(HelloWorldActor.create(), "helloWorldSystem");
        final ActorSystem<GreeterActor.SayHello> greeterSystem = ActorSystem.create(GreeterActor.create(), "greeterSystem");

        // GreeterActor에게 HelloWorldActor의 ActorRef를 넘겨줌
        greeterSystem.tell(new GreeterActor.SayHello("hello kiki", helloWorldSystem));

    }
}
