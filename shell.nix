let
  pkgs = import <nixpkgs> {};

in
  pkgs.mkShell {
    buildInputs = with pkgs; [
      less
      vim
      jq.bin
      jdk21
      #ngrok
    ];

    shellHook = ''
      export AI_HOME=${builtins.getEnv "PWD"}
      export SPRING_AI_OPENAI_API_KEY=$OPENAI_API_KEY
      export LANG=en_US.UTF-8
    '';
  }
