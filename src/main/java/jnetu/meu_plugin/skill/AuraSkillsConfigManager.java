package jnetu.meu_plugin.skill;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class AuraSkillsConfigManager {

    private final Plugin plugin;
    private final File auraSkillsMenusDir;

    public AuraSkillsConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.auraSkillsMenusDir = new File(plugin.getServer().getPluginsFolder(), "AuraSkills/menus");
    }

    /**
     * Injeta as configurações customizadas nos menus do AuraSkills
     */
    public void injetarConfiguracoes() {
        if (!auraSkillsMenusDir.exists()) {
            plugin.getLogger().warning("Pasta de menus do AuraSkills não encontrada!");
            plugin.getLogger().warning("O AuraSkills precisa ter sido executado pelo menos uma vez.");
            return;
        }

        try {
            injetarArquivosDeConfiguracao();
            injetarCarismaNoStatInfo();
            injetarChatBatteryNoStatInfo();
            injetarSocialNoLevelProgression();
            plugin.getLogger().info("✅ Configurações injetadas nos menus do AuraSkills!");
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao injetar configurações: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Injeta a stat "Carisma" no arquivo stat_info.yml
     */
    private void injetarCarismaNoStatInfo() throws IOException {
        File statInfoFile = new File(auraSkillsMenusDir, "stat_info.yml");
        if (!statInfoFile.exists()) {
            plugin.getLogger().warning("stat_info.yml não encontrado!");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(statInfoFile);

        String basePathStat = "templates.stat.contexts";

        if (config.contains(basePathStat + ".meu_plugin/carisma")) {
            plugin.getLogger().info("Carisma já está configurado em stat_info.yml");
            return;
        }

        config.set(basePathStat + ".meu_plugin/carisma.material", "player_head");

        String basePathTrait = "templates.trait.contexts";
        if (!config.contains(basePathTrait + ".meu_plugin/chat_battery")) {
            config.set(basePathTrait + ".meu_plugin/chat_battery.material", "clock");
        }

        config.save(statInfoFile);
        plugin.getLogger().info("✓ Carisma e Chat Battery adicionados ao stat_info.yml");
    }

    /**
     * Injeta o trait "Chat Battery" no arquivo stat_info.yml (já feito acima)
     */
    private void injetarChatBatteryNoStatInfo() {
        // Já incluído no método injetarCarismaNoStatInfo()
    }

    /**
     * Injeta a skill "Social" no arquivo level_progression.yml
     */
    private void injetarSocialNoLevelProgression() throws IOException {
        File levelProgressionFile = new File(auraSkillsMenusDir, "level_progression.yml");
        if (!levelProgressionFile.exists()) {
            plugin.getLogger().warning("level_progression.yml não encontrado!");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(levelProgressionFile);

        // Verifica se já existe
        String basePath = "templates.skill.contexts";
        if (config.contains(basePath + ".meu_plugin:social")) {
            plugin.getLogger().info("Social já está configurado em level_progression.yml");
            return;
        }

        // Adiciona o contexto da skill Social
        config.set(basePath + ".social.material", "player_head");
        config.set(basePath + ".meu_plugin:social.material", "player_head");

        config.save(levelProgressionFile);
        plugin.getLogger().info("✓ Skill Social adicionada ao level_progression.yml");
    }

    /**
     * Remove as injeções (para desinstalar o plugin limpo)
     */
    public void removerInjecoes() {
        try {
            removerDoArquivo("stat_info.yml", List.of(
                    "templates.stat.contexts.carisma",
                    "templates.stat.contexts.meu_plugin:carisma",
                    "templates.trait.contexts.chat_battery",
                    "templates.trait.contexts.meu_plugin:chat_battery"
            ));

            removerDoArquivo("level_progression.yml", List.of(
                    "templates.skill.contexts.social",
                    "templates.skill.contexts.meu_plugin:social"
            ));

            plugin.getLogger().info("✓ Injeções removidas dos menus do AuraSkills");
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao remover injeções: " + e.getMessage());
        }
    }

    private void removerDoArquivo(String nomeArquivo, List<String> caminhos) throws IOException {
        File arquivo = new File(auraSkillsMenusDir, nomeArquivo);
        if (!arquivo.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(arquivo);

        for (String caminho : caminhos) {
            config.set(caminho, null);
        }

        config.save(arquivo);
    }


    private void copiarResource(String resourcePath, File destino) throws IOException {
        if (destino.exists()) {
            plugin.getLogger().info("Arquivo já existe, pulando: " + resourcePath);
            return;
        }

        InputStream in = plugin.getResource(resourcePath);
        if (in == null) {
            plugin.getLogger().warning("Resource não encontrado no JAR: " + resourcePath);
            return;
        }

        destino.getParentFile().mkdirs();

        try (InputStream input = in;
             FileOutputStream output = new FileOutputStream(destino)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }

        plugin.getLogger().info("✓ Criado: " + destino.getPath());
    }

    private void injetarArquivosDeConfiguracao() {
        // saveResource(path, replace) — false = não sobrescreve se já existir
        salvarResourceSeNaoExistir("stats.yml");
        salvarResourceSeNaoExistir("sources/social.yml");
        salvarResourceSeNaoExistir("rewards/social.yml");
    }

    private void salvarResourceSeNaoExistir(String resourcePath) {
        File destino = new File(plugin.getDataFolder(), resourcePath);
        if (destino.exists()) {
            plugin.getLogger().info("Arquivo já existe, pulando: " + resourcePath);
            return;
        }
        // Cria subpastas (ex: sources/, rewards/) se necessário
        destino.getParentFile().mkdirs();
        plugin.saveResource(resourcePath, false);
        plugin.getLogger().info("✓ Criado: " + resourcePath);
    }
}
