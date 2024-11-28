let
  pkgs = import <nixpkgs> { config = { allowUnfree = true; }; };

in
  pkgs.mkShell {
    buildInputs = with pkgs; [
      less
      vim
      jq.bin
      jdk21
      ngrok
      oauth2l
    ];

    LANG = "en_US.UTF-8";
    LC_ALL = "en_US.UTF-8";
    LOCALE_ARCHIVE = "${pkgs.glibcLocales}/lib/locale/locale-archive";

    shellHook = ''
      export AI_HOME=${builtins.getEnv "PWD"}
      export SPRING_AI_OPENAI_API_KEY=$OPENAI_API_KEY
      export LANG=en_US.UTF-8
    '';
  }
