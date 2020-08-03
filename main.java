package me.invakid.oliver.discord.command;

import me.invakid.oliver.Main;
import me.invakid.oliver.discord.AzranBot;
import me.invakid.oliver.discord.config.BotConfig;
import me.invakid.oliver.discord.db.DatabaseManager;
import me.invakid.oliver.discord.payment.PaymentManager;
import me.invakid.botcore.BotCore;
import me.invakid.botcore.annotation.Command;
import me.invakid.botcore.util.MessageUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.restaction.InviteAction;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("Duplicates")
public class Commands {

    @Command("clear")
    public static void clearCommand(MessageReceivedEvent event) {
        Message message = event.getMessage();
        TextChannel textChannel = event.getTextChannel();

        MessageHistory history = new MessageHistory(textChannel);

        String[] arguments = MessageUtil.getCommandArgs(message);
        if (arguments.length != 1) return;

        List<Message> m;
        try {
            int messages = Integer.parseInt(arguments[0]);
            m = history.retrievePast(messages + 1).complete();
        } catch (Exception ex) {
            return;
        }

        BotConfig config = AzranBot.INSTANCE.getBotConfig();

        if (config.reloadPerms.contains(event.getMember().getUser().getId())) {
            try {
                textChannel.deleteMessages(m).queue();
            } catch (Exception ignored) {
            }
        }

    }

    @Command("addclient")
    public static void addClient(final MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();
        Message message = event.getMessage();
        Guild az = event.getGuild();
        String[] arguments = MessageUtil.getCommandArgs(message);

        message.delete().queue();
        if (!textChannel.getId().equalsIgnoreCase("492317564303835136")) return;

        if (arguments.length != 1) {
            BotCore.getInstance().sendEmbed("Please use !addclient <id>", 15000, textChannel);
            return;
        }

        Member member = az.getMemberById(arguments[0]);
        if (member == null) {
            BotCore.getInstance().sendEmbed("Specified member not found", 15000, textChannel);
            return;
        }

        Role client = az.getRoleById("492396099215163416");
        az.getController().addSingleRoleToMember(member, client).queue();
        DatabaseManager.INSTANCE.addClients(member);
        BotCore.getInstance().sendEmbed("Done!", 15000, textChannel);
    }

    @Command("addclient30")
    public static void addClient30(final MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();
        Message message = event.getMessage();
        Guild az = event.getGuild();
        String[] arguments = MessageUtil.getCommandArgs(message);

        message.delete().queue();
        if (!textChannel.getId().equalsIgnoreCase("492317564303835136")) return;

        if (arguments.length != 1) {
            BotCore.getInstance().sendEmbed("Please use !addclient30 <id>", 15000, textChannel);
            return;
        }

        Member member = az.getMemberById(arguments[0]);
        if (member == null) {
            BotCore.getInstance().sendEmbed("Specified member not found", 15000, textChannel);
            return;
        }

        Role client = az.getRoleById("492396099215163416");
        az.getController().addSingleRoleToMember(member, client).queue();
        DatabaseManager.INSTANCE.addClientsFor30Days(member);
    }

    @Command("check")
    public static void checkCommand(MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();

        if (AzranBot.INSTANCE.getChannelIDs().containsKey(textChannel.getId())) {
            String[] args = MessageUtil.getCommandArgs(event.getMessage());
            if (args.length != 1) {
                BotCore.getInstance().sendEmbed("Please use !check <ign>", -1, textChannel);
                return;
            }
            String ign = args[0];

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("AzranBot Response");
            builder.setFooter("Azran © 2018", "https://cdn.discordapp.com/avatars/396445617179852810/fa902d997f490b3a38b826c2c478f7b9.png");

            ArrayList<String> detections = DatabaseManager.INSTANCE.getDetectionsByIgn(ign);
            if (detections.isEmpty()) {
                builder.setColor(Color.GREEN);
                builder.setDescription("The player " + ign + " has never been caught by Azran ;(");
                textChannel.sendMessage(builder.build()).queue();
            } else {
                builder.setColor(Color.RED);
                builder.setDescription("The player " + ign + " has been caught cheating by Azran on " + detections.get(1) + " with " + detections.get(0));
                textChannel.sendMessage(builder.build()).queue();
            }
        }
    }

    @Command("download")
    public static void downloadCommand(MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();

        BotConfig config = AzranBot.INSTANCE.getBotConfig();
        if (AzranBot.INSTANCE.getChannelIDs().containsKey(textChannel.getId())) {
            String token = DatabaseManager.INSTANCE.generateAndAddToken();
            BotCore.getInstance().sendEmbed(config.getDownloadMessage().replaceAll("%token%", token), -1, textChannel);
        } else if (event.getGuild().getId().equalsIgnoreCase("492023601457922049")) {
            BotCore.getInstance().sendEmbed(
                    "You must buy azran to be able to download it! Please go to " +
                            event.getGuild().getTextChannelById("492316856561041418").getAsMention() +
                            " and type in -new to open a ticket for purchase", -1, textChannel);
        }
    }

    @Command("leave")
    public static void leaveCommand(MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();

        BotConfig config = AzranBot.INSTANCE.getBotConfig();

        if (config.reloadPerms.contains(event.getMember().getUser().getId())) {
            Set<String> guilds = AzranBot.INSTANCE.getGuilds().keySet();
            AzranBot.INSTANCE.getBotCore().getJda().getGuilds().forEach(guild -> {
                if (guild == null || guild.getId().equalsIgnoreCase("492023601457922049")) return;
                try {
                    if (!guilds.contains(guild.getId())) {
                        guild.leave().complete();
                        BotCore.getInstance().sendEmbed(String.format("Successfully left from %s!", guild.getName()), -1, textChannel);
                    }
                } catch (Exception ignored) {
                }
            });
        }
    }

    @Command("code")
    public static void codeCmd(final MessageReceivedEvent event) {
        pinCommand(event);
    }

    @Command("pin")
    public static void pinCommand(final MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();

        BotConfig config = AzranBot.INSTANCE.getBotConfig();
        if (AzranBot.INSTANCE.getChannelIDs().containsKey(textChannel.getId()))
            BotCore.getInstance().sendEmbed(config.getPinGenerated().replaceAll("%pin%", DatabaseManager.INSTANCE.generateAndAddPIN(textChannel, event.getMember().getUser())), -1, textChannel);
        else if (event.getGuild().getId().equalsIgnoreCase("492023601457922049")) {
            BotCore.getInstance().sendEmbed(
                    "You must buy azran to be able to use it! Please go to " +
                            event.getGuild().getTextChannelById("492316856561041418").getAsMention() +
                            " and type in -new to open a ticket for purchase", -1, textChannel);
        }
    }

    @Command("pw")
    public static void pwCommand(final MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();
        event.getMessage().delete().queue();
        if (textChannel.getId().equalsIgnoreCase("492317564303835136"))
            BotCore.getInstance().sendEmbed("Your password is: " + DatabaseManager.INSTANCE.generateAndAddPW(event.getMember().getUser()), 15000, textChannel);
    }

    @Command("reload")
    public static void reloadCommand(MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();

        BotConfig config = AzranBot.INSTANCE.getBotConfig();
        if (AzranBot.INSTANCE.getChannelIDs().containsKey(textChannel.getId()) && config.reloadPerms.contains(event.getMember().getUser().getId()))
            try {
                Main.reload();
                BotCore.getInstance().sendEmbed(config.getReloadSuccess(), -1, textChannel);
            } catch (Exception ex) {
                ex.printStackTrace();
                BotCore.getInstance().sendEmbed(config.getReloadFailure(), -1, textChannel);
            }

    }

    @Command("addguild")
    public static void addGuild(final MessageReceivedEvent event) {
        TextChannel adminChannel = event.getTextChannel();
        Message message = event.getMessage();
        Guild az = event.getGuild();
        String[] arguments = MessageUtil.getCommandArgs(message);

        message.delete().queue();
        if (!adminChannel.getId().equalsIgnoreCase("492317564303835136")) return;

        if (arguments.length != 3) {
            BotCore.getInstance().sendEmbed("Please use !addguild <guildId> <channelId> <prefix>", 15000, adminChannel);
            return;
        }

        Guild guild = event.getJDA().getGuildById(arguments[0]);
        if (guild == null) {
            BotCore.getInstance().sendEmbed("Guild not found", 15000, adminChannel);
            return;
        }
        TextChannel channel = guild.getTextChannelById(arguments[1]);
        if (channel == null) {
            BotCore.getInstance().sendEmbed("Text channel not found", 15000, adminChannel);
            return;
        }

        DatabaseManager.INSTANCE.addGuild(guild, channel, arguments[2]);
        BotCore.getInstance().sendEmbed("Successfully added guild " + guild.getName() + "'s channel " + channel.getName() + " to our partnered list with prefix " + arguments[2], -1, adminChannel);
        try {
            Main.reload();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Command("removeguild")
    public static void removeGuild(final MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();
        Message message = event.getMessage();
        String[] arguments = MessageUtil.getCommandArgs(message);

        message.delete().queue();
        if (!textChannel.getId().equalsIgnoreCase("492317564303835136")) return;

        if (arguments.length != 1) {
            BotCore.getInstance().sendEmbed("Please use !removeguild <guildId>", 15000, textChannel);
            return;
        }

        Guild guild = event.getJDA().getGuildById(arguments[0]);
        if (guild == null) {
            BotCore.getInstance().sendEmbed("Guild not found", 15000, textChannel);
            return;
        }
        DatabaseManager.INSTANCE.removeGuild(guild.getId());
        BotCore.getInstance().sendEmbed("Successfully removed guild " + guild.getName(), -1, textChannel);
        try {
            Main.reload();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Command("getinvite")
    public static void getInvite(final MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();
        Message message = event.getMessage();
        String[] arguments = MessageUtil.getCommandArgs(message);

        message.delete().queue();
        if (!textChannel.getId().equalsIgnoreCase("492317564303835136")) return;

        if (arguments.length != 1) {
            BotCore.getInstance().sendEmbed("Please use !getinvite <guildId>", 15000, textChannel);
            return;
        }

        Guild guild = event.getJDA().getGuildById(arguments[0]);
        if (guild == null) {
            BotCore.getInstance().sendEmbed("Guild not found", 15000, textChannel);
            return;
        }


        Invite complete = null;

        for (TextChannel channel : guild.getTextChannels()) {

            try {
                InviteAction invite = channel.createInvite();
                invite.setMaxUses(1);
                invite.setTemporary(true);
                complete = invite.complete();
            } catch (Exception ignored) {
            }

            if (!complete.getURL().isEmpty()) break;
        }

        BotCore.getInstance().sendEmbed(complete.getURL(), 15000, textChannel);

        try {
            Main.reload();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Command("pay")
    public static void onCommand(MessageReceivedEvent ev) {
        TextChannel textChannel = ev.getTextChannel();
        if (!textChannel.getName().startsWith("ticket")) return;
        EmbedBuilder ebe = new EmbedBuilder();
        ebe.setTitle("AzranBot Response");
        ebe.setColor(Color.WHITE);
        ebe.setFooter("Azran © 2019", "https://cdn.discordapp.com/avatars/396445617179852810/fa902d997f490b3a38b826c2c478f7b9.png");
        String[] args = MessageUtil.getCommandArgs(ev.getMessage());
        String msg = args[0];
        float fl = ((Float.parseFloat(msg) * 3F / 100F) + 0.30F) + Float.parseFloat(msg);
        String kek = new DecimalFormat("#.##").format(fl).replace(",", ".");

        String line = null;
        try {
            URL url = new URL("https://azran.info/generate?ticketId=" + textChannel.getId() + "&price=" + kek + "&ticketName=" + textChannel.getName());

            InputStream inputStream = url.openStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            line = bufferedReader.lines().filter(s -> s.contains("https")).findFirst().get();
            bufferedReader.close();
            inputStream.close();
            inputStreamReader.close();

            line = line.replaceAll("<p>", "").replaceAll("</p>", "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (line == null) {
            ebe.setDescription("There was error with generating transaction!");
        } else {
            ebe.setTitle("Azran - PAYMENT - Click me to proceed to the payment!", line);
            ebe.setDescription("Please send $" + kek + " (fees included) using this link ^^^ to proceed to the payment!");
        }

        textChannel.sendMessage(ebe.build()).complete();
    }

    @Command("transaction")
    public static void onTransactionCommand(MessageReceivedEvent ev) {
        if (!ev.getTextChannel().getName().startsWith("ticket")) return;
        EmbedBuilder ebe = new EmbedBuilder();
        ebe.setTitle("AzranBot Response");
        ebe.setColor(Color.WHITE);
        ebe.setFooter("Azran © 2019", "https://cdn.discordapp.com/avatars/396445617179852810/fa902d997f490b3a38b826c2c478f7b9.png");
        String[] args = MessageUtil.getCommandArgs(ev.getMessage());
        if (args.length != 1) {
            ebe.setDescription("Please use /transaction [transactionID]");
            ev.getTextChannel().sendMessage(ebe.build()).queue();
            return;
        }
        String transactionID = args[0];
        String response = PaymentManager.checkPayment(transactionID);
        if (response.contains("Error")) {
            ebe.setDescription("An error occured while trying to check the transaction! Are you sure that's the right transaction id?");
            ev.getTextChannel().sendMessage(ebe.build()).queue();
        } else {
            String[] info = response.split(":");
            ebe.setDescription("Payment of " + info[1].trim() + "'s status is: " + info[0].trim());
            ev.getTextChannel().sendMessage(ebe.build()).queue();
        }
    }

    @Command("verify")
    public static void verifyTransaction(MessageReceivedEvent ev) {
        TextChannel textChannel = ev.getTextChannel();
        if (!textChannel.getName().startsWith("ticket")) return;

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("AzranBot Response");
        builder.setFooter("Azran © 2018", "https://cdn.discordapp.com/avatars/396445617179852810/fa902d997f490b3a38b826c2c478f7b9.png");

        ArrayList<String> data = DatabaseManager.INSTANCE.getTransactionData(textChannel.getId());
        if (data.isEmpty()) {
            builder.setColor(Color.GREEN);
            builder.setDescription("There is no information about this ticket");
            textChannel.sendMessage(builder.build()).queue();
        } else {
            boolean status = Integer.parseInt(data.get(2)) == 1;
            if (status) {
                builder.setColor(Color.GREEN);
                builder.setDescription("The status of " + data.get(0) + "'s " + data.get(1) + "$ transaction is : payment complete");
            } else {
                builder.setColor(Color.RED);
                builder.setDescription("The status of " + data.get(0) + "'s " + data.get(1) + "$ transaction is : payment not complete");
            }
            textChannel.sendMessage(builder.build()).queue();
        }
    }

}
