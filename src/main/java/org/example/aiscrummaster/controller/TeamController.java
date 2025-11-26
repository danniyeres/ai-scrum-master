package org.example.aiscrummaster.controller;

import lombok.RequiredArgsConstructor;
import org.example.aiscrummaster.dto.TeamDto;
import org.example.aiscrummaster.dto.TeamMemberDto;
import org.example.aiscrummaster.model.TeamMember;
import org.example.aiscrummaster.service.TeamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;


    @PostMapping
    public TeamDto createTeamWithMembers(@RequestBody TeamDto teamDto) {
        List<TeamMember> members = teamDto.getTeamMembers();
        return teamService.createTeamWithMembers(teamDto.getName(), members);
    }

    @GetMapping
    public List<TeamDto> getAllTeams() {
        return teamService.getAllTeams();
    }

    @PostMapping("/members")
    public TeamMemberDto addMemberToTeam(@RequestParam Long teamId, @RequestBody TeamMember newMember) {
        return teamService.addMemberToTeam(teamId, newMember);
    }

    @DeleteMapping("/members")
    public void removeMemberFromTeam(@RequestParam Long memberId) {
        teamService.removeMemberFromTeam(memberId);
    }
}
